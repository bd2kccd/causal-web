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
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraph;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphUtil;
import edu.pitt.dbmi.ccd.commons.security.WebSecurityDSA;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.dto.response.FileInfoResponse;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import edu.pitt.dbmi.ccd.web.service.RestRequestService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
 * Sep 8, 2015 11:07:16 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopResultComparisonService extends AbstractResultComparisonService implements ResultComparisonService, RestRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopResultComparisonService.class);

    private final String resultUrl;

    private final String comparisonPath;

    private final String algorithPath;

    private final String appId;

    private final RestTemplate restTemplate;

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public DesktopResultComparisonService(
            @Value("${ccd.rest.url.result}") String resultUrl,
            @Value("${ccd.rest.path.result.comparison:/algorithm/comparison}") String comparisonPath,
            @Value("${ccd.rest.path.result.algorithm:/algorithm}") String algorithPath,
            @Value("${ccd.rest.appId}") String appId,
            RestTemplate restTemplate,
            UserAccountService userAccountService) {
        this.resultUrl = resultUrl;
        this.comparisonPath = comparisonPath;
        this.algorithPath = algorithPath;
        this.appId = appId;
        this.restTemplate = restTemplate;
        this.userAccountService = userAccountService;
    }

    @Override
    public List<ResultFileInfo> list(AppUser appUser) {
        List<ResultFileInfo> resultFileInfos = new LinkedList<>();

        try {
            List<ResultFileInfo> results = new LinkedList<>();
            results.addAll(listLocalResultFileInfo(appUser.getResultComparisonDir()));
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
    public List<SimpleGraph> compareResultFile(List<String> fileNames, AppUser appUser) {
        List<SimpleGraph> graphs = new LinkedList<>();

        List<String> remoteFileNames = new LinkedList<>();
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
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
            byte[] cloudData = downloadRemoteAlgoResultFile(appUser.getUsername(), fileName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
                graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        });

        return graphs;
    }

    @Override
    public void writeResultComparison(ResultComparison resultComparison, String fileNameOut, AppUser appUser) {
        Path file = Paths.get(appUser.getResultComparisonDir(), fileNameOut);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
            writeResultComparison(writer, resultComparison);
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to write file '%s'.", fileNameOut), exception);
        }
    }

    @Override
    public void delete(List<String> fileNames, AppUser appUser) {
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

        if (!remoteFileNames.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                HttpEntity<?> entity = new HttpEntity<>(remoteFileNames, headers);

                URI url = UriComponentsBuilder.fromHttpUrl(this.resultUrl + this.comparisonPath)
                        .queryParam("usr", appUser.getUsername())
                        .queryParam("appId", this.appId)
                        .build().toUri();

                restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            } catch (RestClientException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    @Override
    public void download(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response) {
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

    private byte[] downloadRemoteFile(String username, String fileName) {
        byte[] data = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.TEXT_PLAIN_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            URI url = UriComponentsBuilder.fromHttpUrl(this.resultUrl + this.comparisonPath + "/" + fileName + "/")
                    .queryParam("usr", username)
                    .queryParam("appId", this.appId)
                    .build().toUri();

            ResponseEntity<ByteArrayResource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ByteArrayResource.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                ByteArrayResource byteArrayResource = responseEntity.getBody();
                data = byteArrayResource.getByteArray();
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return data;
    }

    private byte[] downloadRemoteAlgoResultFile(String username, String fileName) {
        byte[] data = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.TEXT_PLAIN_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            URI url = UriComponentsBuilder.fromHttpUrl(this.resultUrl + this.algorithPath + "/" + fileName + "/")
                    .queryParam("usr", username)
                    .queryParam("appId", this.appId)
                    .build().toUri();

            ResponseEntity<ByteArrayResource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ByteArrayResource.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                ByteArrayResource byteArrayResource = responseEntity.getBody();
                data = byteArrayResource.getByteArray();
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return data;
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
                    .pathSegment(this.comparisonPath, "list")
                    .build().toUri();

            String signature = WebSecurityDSA.createSignature(uri.toString(), userAccount.getPrivateKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setDate(System.currentTimeMillis());
            headers.set(HEADER_APP_ID, Base64.getEncoder().encodeToString(this.appId.getBytes()));
            headers.set(HEADER_ACCOUNT_ID, Base64.getEncoder().encodeToString(accountId.getBytes()));
            headers.set(HEADER_SIGNATURE, signature);

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

}
