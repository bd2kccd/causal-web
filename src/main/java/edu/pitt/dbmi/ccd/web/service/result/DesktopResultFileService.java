/*
 * Copyright (C) 2015 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.web.service.result;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraph;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphUtil;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Sep 5, 2015 8:11:42 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopResultFileService extends AbstractResultFileService implements ResultFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopResultFileService.class);

    private final String appId;

    private final String userResultsUri;

    private final String userResultFileDownloadUri;

    private final String userResultFileDeleteUri;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public DesktopResultFileService(
            @Value("${ccd.rest.appId:1}") String appId,
            @Value("${ccd.results.usr.uri:http://localhost:8080/ccd-ws/algorithm/results/usr}") String userResultsUri,
            @Value("${ccd.results.file.usr.uri:http://localhost:8080/ccd-ws/algorithm/results/file/usr}") String userResultFileDownloadUri,
            @Value("${ccd.results.file.delete.usr.uri:http://localhost:8080/ccd-ws/algorithm/results/file/delete/usr}") String userResultFileDeleteUri,
            RestTemplate restTemplate) {
        this.appId = appId;
        this.userResultsUri = userResultsUri;
        this.userResultFileDownloadUri = userResultFileDownloadUri;
        this.userResultFileDeleteUri = userResultFileDeleteUri;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ResultFileInfo> getUserResultFiles(final AppUser appUser) {
        List<ResultFileInfo> resultFileInfos = new LinkedList<>();
        try {
            List<ResultFileInfo> results = new LinkedList<>();
            results.addAll(getUserLocalResultFiles(appUser));
            results.addAll(getUserRemoteResultFiles(appUser));

            ResultFileInfo[] fileInfos = results.toArray(new ResultFileInfo[results.size()]);

            Arrays.sort(fileInfos, Collections.reverseOrder());  // sort

            resultFileInfos.addAll(Arrays.asList(fileInfos));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return resultFileInfos;
    }

    @Override
    public void downloadResultFile(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        if (remote) {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            try (ReadableByteChannel inputChannel = Channels.newChannel(new ByteArrayInputStream(cloudData));
                    WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {
                final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
                while (inputChannel.read(buffer) != -1) {
                    // prepare the buffer to be drained
                    buffer.flip();
                    // write to the channel, may block
                    outputChannel.write(buffer);
                    // If partial transfer, shift remainder down
                    // If buffer is empty, same as doing clear()
                    buffer.compact();
                }
                // EOF will leave buffer in fill state
                buffer.flip();
                // make sure the buffer is fully drained.
                while (buffer.hasRemaining()) {
                    outputChannel.write(buffer);
                }
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to download file '%s'.", fileName), exception);
            }
        } else {
            try {
                downloadLocalResultFile(fileName, appUser, request, response);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to download file '%s'.", fileName), exception);
            }
        }
    }

    @Override
    public Map<String, String> getPlotParameters(String fileName, boolean remote, AppUser appUser) {
        Map<String, String> parameters = new TreeMap<>();

        if (remote) {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                extractParameters(reader, parameters);
            } catch (IOException exception) {

            }
        } else {
            Path file = Paths.get(appUser.getResultDirectory(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                extractParameters(reader, parameters);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return parameters;
    }

    @Override
    public List<Node> getGraphNodes(String fileName, boolean remote, AppUser appUser) {
        List<Node> nodes = new LinkedList<>();

        if (remote) {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                extractNodes(reader, nodes);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        } else {
            Path file = Paths.get(appUser.getResultDirectory(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                extractNodes(reader, nodes);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return nodes;
    }

    @Override
    public List<String> getErrorMessages(String fileName, boolean remote, AppUser appUser) {
        List<String> errorMsg = new LinkedList<>();

        if (remote) {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                readInErrorMessage(reader, errorMsg);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        } else {
            Path file = Paths.get(appUser.getResultDirectory(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                readInErrorMessage(reader, errorMsg);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return errorMsg;
    }

    @Override
    public void deleteResultFile(List<String> fileNames, AppUser appUser) {
        List<String> remoteFileNames = new LinkedList<>();
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getResultDirectory(), fileName);
            if (Files.exists(file)) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage());
                }
            } else {
                remoteFileNames.add(fileName);
            }
        });

        String preUrl = String.format("%s/%s?appId=%s&fileName=", userResultFileDeleteUri, appUser.getUsername(), appId);
        remoteFileNames.forEach(fileName -> {
            restTemplate.delete(preUrl + fileName);
        });
    }

    @Override
    public List<SimpleGraph> compareResultFile(List<String> fileNames, AppUser appUser) {
        List<SimpleGraph> graphs = new LinkedList<>();

        List<String> remoteFileNames = new LinkedList<>();
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getResultDirectory(), fileName);
            if (Files.exists(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                    graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            } else {
                remoteFileNames.add(fileName);
            }
        });

        remoteFileNames.forEach(fileName -> {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        });

        return graphs;
    }

    private List<ResultFileInfo> getUserRemoteResultFiles(AppUser appUser) {
        List<ResultFileInfo> list = new LinkedList<>();

        String[] keys = {"fileName", "size", "creationDate"};
        try {
            ResponseEntity<List> entity = restTemplate.getForEntity(String.format("%s/%s?appId=%s", userResultsUri, appUser.getUsername(), appId), List.class);
            List response = entity.getBody();
            response.forEach(i -> {
                Map map = (Map) i;
                String filename = (String) map.get(keys[0]);
                Integer size = (Integer) map.get(keys[1]);
                Long creationTime = (Long) map.get(keys[2]);

                ResultFileInfo info = new ResultFileInfo();
                info.setFileName(filename);
                info.setSize(FilePrint.humanReadableSize(size, true));
                info.setCreationDate(FilePrint.fileTimestamp(creationTime));
                info.setRawCreationDate(creationTime);
                info.setOnCloud(true);
                info.setError(filename.startsWith("error"));

                list.add(info);
            });
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return list;
    }

    private byte[] downloadRemoteFile(String username, String fileName) {
        String uri = String.format("%s/%s?appId=%s&fileName=%s", userResultFileDownloadUri, username, appId, fileName);
        ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity(uri, ByteArrayResource.class);

        byte[] data = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            ByteArrayResource byteArrayResource = response.getBody();
            data = byteArrayResource.getByteArray();
        }

        return data;
    }

}
