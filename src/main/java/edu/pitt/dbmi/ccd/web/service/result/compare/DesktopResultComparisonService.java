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
package edu.pitt.dbmi.ccd.web.service.result.compare;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
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
 * Sep 8, 2015 11:07:16 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopResultComparisonService extends AbstractResultComparisonService implements ResultComparisonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopResultComparisonService.class);

    private final String appId;

    private final String resultFileComparisonUri;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public DesktopResultComparisonService(
            @Value("${ccd.rest.appId:1}") String appId,
            @Value("${ccd.result.comparison.uri:http://localhost:8080/ccd-ws/algorithm/result/comparison}") String resultFileComparisonUri,
            RestTemplate restTemplate) {
        this.appId = appId;
        this.resultFileComparisonUri = resultFileComparisonUri;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ResultFileInfo> getUserResultComparisonFiles(AppUser appUser) {
        List<ResultFileInfo> resultFileInfos = new LinkedList<>();

        try {
            List<ResultFileInfo> results = new LinkedList<>();
            results.addAll(getUserLocalResultComparisonFiles(appUser));
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
    public ResultComparison readInResultComparisonFile(String fileName, boolean remote, AppUser appUser) {
        ResultComparison resultComparison = new ResultComparison(fileName);

        if (remote) {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                extractResultComparison(reader, resultComparison);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
            resultComparison.setRemote(true);
        } else {
            Path file = Paths.get(appUser.getResultComparisonDir(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                extractResultComparison(reader, resultComparison);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return resultComparison;
    }

    @Override
    public void downloadResultComparisonFile(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response) {
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
    public void deleteResultComparisonFile(List<String> fileNames, AppUser appUser) {
        List<String> remoteFileNames = new LinkedList<>();
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getResultComparisonDir(), fileName);
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

        String uri = String.format("%s/usr/%s/%s/?appId=%s", resultFileComparisonUri, appUser.getUsername(), "%s", appId);
        remoteFileNames.forEach(fileName -> {
            restTemplate.delete(String.format(uri, fileName));
        });
    }

    private byte[] downloadRemoteFile(String username, String fileName) {
        String uri = String.format("%s/usr/%s/%s/?appId=%s", resultFileComparisonUri, username, fileName, appId);
        ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity(uri, ByteArrayResource.class);

        byte[] data = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            ByteArrayResource byteArrayResource = response.getBody();
            data = byteArrayResource.getByteArray();
        }

        return data;
    }

    private List<ResultFileInfo> getUserRemoteResultFiles(AppUser appUser) {
        List<ResultFileInfo> list = new LinkedList<>();

        String[] keys = {"fileName", "size", "creationDate"};
        try {
            String uri = String.format("%s/usr/%s?appId=%s", resultFileComparisonUri, appUser.getUsername(), appId);
            ResponseEntity<List> entity = restTemplate.getForEntity(uri, List.class);
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

}
