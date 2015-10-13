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
package edu.pitt.dbmi.ccd.web.service.result.algorithm;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.security.WebSecurityDSA;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.dto.response.FileInfoResponse;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Sep 15, 2015 12:16:38 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopAlgorithmResultService extends AbstractAlgorithmResultService implements AlgorithmResultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopAlgorithmResultService.class);

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final String resultUrl;

    private final String algorithPath;

    private final String appId;

    private final RestTemplate restTemplate;

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public DesktopAlgorithmResultService(
            @Value("${ccd.rest.url.result}") String resultUrl,
            @Value("${ccd.rest.appId}") String appId,
            RestTemplate restTemplate,
            UserAccountService userAccountService) {
        this.resultUrl = resultUrl;
        this.algorithPath = "algorithm";
        this.appId = appId;
        this.restTemplate = restTemplate;
        this.userAccountService = userAccountService;
    }

    @Override
    public List<ResultFileInfo> listResultFileInfo(final AppUser appUser) {
        List<ResultFileInfo> resultFileInfos = new LinkedList<>();
        try {
            List<ResultFileInfo> results = new LinkedList<>();
            results.addAll(listLocalResultFileInfo(appUser.getAlgoResultDir()));
            results.addAll(listRemoteResultFileInfo(appUser.getUsername()));

            ResultFileInfo[] fileInfos = results.toArray(new ResultFileInfo[results.size()]);

            Arrays.sort(fileInfos, Collections.reverseOrder());  // sort

            resultFileInfos.addAll(Arrays.asList(fileInfos));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return resultFileInfos;
    }

    @Override
    public void deleteResultFile(List<String> fileNames, AppUser appUser) {
        List<String> remoteFileNames = new LinkedList<>();
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
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

        if (!remoteFileNames.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                HttpEntity<?> entity = new HttpEntity<>(remoteFileNames, headers);

                URI url = UriComponentsBuilder.fromHttpUrl(this.resultUrl + this.algorithPath)
                        .queryParam("usr", appUser.getUsername())
                        .queryParam("appId", this.appId)
                        .build().toUri();

                restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            } catch (RestClientException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    private List<ResultFileInfo> listRemoteResultFileInfo(String username) {
        List<ResultFileInfo> list = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);
        String accountId = userAccount.getAccountId();
        if (accountId == null) {
            return list;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(this.resultUrl)
                    .pathSegment(this.algorithPath)
                    .build().toUri();

            String signature = WebSecurityDSA.createSignature(uri.toString(), userAccount.getPrivateKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setDate(System.currentTimeMillis());
            headers.set("appId", this.appId);
            headers.set("accountId", accountId);
            headers.set("signature", signature);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<FileInfoResponse[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, FileInfoResponse[].class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                FileInfoResponse[] responseList = responseEntity.getBody();
                for (FileInfoResponse response : responseList) {
                    String fileName = response.getFileName();
                    Long size = response.getSize();
                    Long creationDate = response.getCreationDate();

                    ResultFileInfo info = new ResultFileInfo();
                    info.setFileName(fileName);
                    info.setSize(FilePrint.humanReadableSize(size, true));
                    info.setCreationDate(FilePrint.fileTimestamp(creationDate));
                    info.setRawCreationDate(creationDate);
                    info.setOnCloud(true);
                    info.setError(fileName.startsWith("error"));

                    list.add(info);
                }
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return list;
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

    private byte[] downloadRemoteFile(String username, String fileName) {
        byte[] data = EMPTY_BYTE_ARRAY;

        UserAccount userAccount = userAccountService.findByUsername(username);
        String accountId = userAccount.getAccountId();
        if (accountId == null) {
            return data;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(this.resultUrl)
                    .pathSegment(this.algorithPath)
                    .pathSegment(fileName)
                    .build().toUri();

            String signature = WebSecurityDSA.createSignature(uri.toString(), userAccount.getPrivateKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
            headers.setDate(System.currentTimeMillis());
            headers.set("appId", this.appId);
            headers.set("accountId", accountId);
            headers.set("signature", signature);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ByteArrayResource> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, ByteArrayResource.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                ByteArrayResource byteArrayResource = responseEntity.getBody();
                data = byteArrayResource.getByteArray();
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return data;
    }

    @Override
    public Map<String, String> getPlotParameters(String fileName, boolean remote, AppUser appUser) {
        Map<String, String> parameters = new TreeMap<>();

        if (remote) {
            byte[] cloudData = downloadRemoteFile(appUser.getUsername(), fileName);
            if (cloudData != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                    extractParameters(reader, parameters);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            }
        } else {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
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
            if (cloudData != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                    extractNodes(reader, nodes);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            }
        } else {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
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
            if (cloudData == null) {
                errorMsg.add("Unable to download file from server.");
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                    readInErrorMessage(reader, errorMsg);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            }
        } else {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                readInErrorMessage(reader, errorMsg);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return errorMsg;
    }

}
