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
package edu.pitt.dbmi.ccd.web.ctrl.data;

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Sep 22, 2015 12:59:11 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "data/upload/remote")
public class RemoteDataUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDataUploadController.class);

    private final String appId;

    private final String dataUrl;

    private final int simultaneousUpload;

    private final Map<String, ChunkUpload> fileUploadMap;

    private final ExecutorService executorService;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public RemoteDataUploadController(
            @Value("${ccd.rest.appId:1}") String appId,
            @Value("${ccd.rest.url.data:http://localhost:9000/ccd-ws/data}") String dataUrl,
            @Value("${ccd.data.upload.simultaneousFileUploads:1}") int simultaneousUpload) {
        this.appId = appId;
        this.dataUrl = dataUrl + "/chunk";
        this.simultaneousUpload = simultaneousUpload;
        this.fileUploadMap = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(simultaneousUpload);
        this.restTemplate = new RestTemplate();

        FormHttpMessageConverter converter = new FormHttpMessageConverter();
        this.restTemplate.getMessageConverters().add(converter);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> uploadStatus(
            @RequestParam("fileName") String fileName,
            @ModelAttribute("appUser") AppUser appUser) {
        ChunkUpload chunkUpload = fileUploadMap.get(fileName);
        if (chunkUpload == null) {
            return ResponseEntity.ok(100);
        } else {
            return ResponseEntity.ok((int) (chunkUpload.progress * 100));
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> startUpload(
            @RequestParam("fileName") String fileName,
            @ModelAttribute("appUser") AppUser appUser) {
        Path file = Paths.get(appUser.getDataDirectory(), fileName);
        if (Files.exists(file)) {
            long chunkSize = 1024 * 1024;
            ChunkUpload chunkUpload = new ChunkUpload(file, chunkSize, appUser.getUsername(), appId, dataUrl, restTemplate);
            fileUploadMap.put(fileName, chunkUpload);
            executorService.execute(chunkUpload);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private class ChunkUpload implements Runnable {

        private final Path file;

        private final long chunkSize;

        private final String username;

        private final String appId;

        private final String restUrl;

        private final RestTemplate restTemplate;

        private double progress;

        public ChunkUpload(Path file, long chunkSize, String username, String appId, String restUrl, RestTemplate restTemplate) {
            this.file = file;
            this.chunkSize = chunkSize;
            this.username = username;
            this.appId = appId;
            this.restUrl = restUrl;
            this.restTemplate = restTemplate;
        }

        @Override
        public void run() {
            progress = 0;
            try {
                String fileName = file.getFileName().toString();
                long fileSize = Files.size(file);
                long maxOffset = Math.max(Math.round(fileSize / this.chunkSize), 1);
                long resumableChunkSize = this.chunkSize;
                long resumableTotalSize = fileSize;

                long resumableTotalChunks = maxOffset;
                String resumableIdentifier = String.format("%d-%s", fileSize, fileName.replaceAll("/[^0-9a-zA-Z_-]/img", ""));
                String resumableFilename = fileName;
                String resumableRelativePath = fileName;
                String resumableType = Files.probeContentType(file);

                try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file))) {
                    for (long offset = 0; offset < maxOffset; offset++) {
                        long startByte = offset * this.chunkSize;
                        long endByte = Math.min(fileSize, (offset + 1) * this.chunkSize);
                        if (fileSize - endByte < this.chunkSize) {
                            endByte = fileSize;
                        }
                        long resumableChunkNumber = offset + 1;
                        long resumableCurrentChunkSize = endByte - startByte;

                        HttpStatus httpStatus;
                        try {
                            URI url = UriComponentsBuilder.fromHttpUrl(this.restUrl)
                                    .queryParam("usr", this.username)
                                    .queryParam("appId", this.appId)
                                    .queryParam("resumableIdentifier", resumableIdentifier)
                                    .queryParam("resumableChunkNumber", Long.toString(resumableChunkNumber))
                                    .queryParam("resumableChunkSize", Long.toString(resumableChunkSize))
                                    .build().toUri();

                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);

                            restTemplate.getForEntity(url, String.class);
                            httpStatus = HttpStatus.OK;
                        } catch (HttpClientErrorException exception) {
                            httpStatus = exception.getStatusCode();
                        }

                        if (httpStatus == HttpStatus.NOT_FOUND) {
                            int readSize = (int) resumableCurrentChunkSize;
                            byte[] byteChunkPart = new byte[readSize];
                            inputStream.read(byteChunkPart, 0, readSize);

                            ByteArrayResource data = new ByteArrayResource(byteChunkPart) {
                                @Override
                                public String getFilename() {
                                    return fileName;
                                }
                            };

                            MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
                            valueMap.add("file", data);
                            valueMap.add("resumableChunkNumber", resumableChunkNumber);
                            valueMap.add("resumableChunkSize", chunkSize);
                            valueMap.add("resumableCurrentChunkSize", resumableCurrentChunkSize);
                            valueMap.add("resumableFilename", resumableFilename);
                            valueMap.add("resumableIdentifier", resumableIdentifier);
                            valueMap.add("resumableRelativePath", resumableRelativePath);
                            valueMap.add("resumableTotalChunks", resumableTotalChunks);
                            valueMap.add("resumableTotalSize", resumableTotalSize);
                            valueMap.add("resumableType", resumableType);

                            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(valueMap);

                            URI url = UriComponentsBuilder.fromHttpUrl(this.restUrl)
                                    .queryParam("usr", this.username)
                                    .queryParam("appId", this.appId)
                                    .build().toUri();
                            try {
                                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                                progress = (offset + 1.0) / maxOffset;
                            } catch (HttpClientErrorException exception) {
                                exception.printStackTrace(System.err);
                            }
                        }
                    }
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage());
                }
                fileUploadMap.remove(fileName);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        public Path getFile() {
            return file;
        }

        public long getChunkSize() {
            return chunkSize;
        }

        public double getProgress() {
            return progress;
        }

    }

}
