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

import edu.pitt.dbmi.ccd.commons.security.WebSecurityDSA;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.dto.request.DatasetUploadRequest;
import edu.pitt.dbmi.ccd.web.service.RestRequestService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final String dataUrl;

    private final Map<String, ChunkUpload> fileUploadMap;

    private final Map<String, ChunkUpload> trashFileUploadMap;

    private final ExecutorService executorService;

    private final RestTemplate restTemplate;

    private final UserAccountService userAccountService;

    @Autowired
    public RemoteDataUploadController(
            @Value("${ccd.rest.url.data:http://localhost:9000/ccd-ws/api/v1.0/account/{accountId}/data/file}") String dataUrl,
            @Value("${ccd.data.upload.simultaneousFileUploads:1}") int simultaneousUpload,
            UserAccountService userAccountService) {
        this.dataUrl = dataUrl;
        this.fileUploadMap = new HashMap<>();
        this.trashFileUploadMap = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(simultaneousUpload);;
        this.restTemplate = new RestTemplate();
        this.userAccountService = userAccountService;

        this.restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public synchronized ResponseEntity<?> getJobsInQueue() {
        List<UploadStatus> status = new LinkedList<>();

        Set<String> keySet = fileUploadMap.keySet();
        keySet.forEach(key -> {
            status.add(new UploadStatus(key, fileUploadMap.get(key).isSuspended()));
        });

        if (!trashFileUploadMap.isEmpty()) {
            List<String> keys = new LinkedList<>();
            keySet = trashFileUploadMap.keySet();
            keySet.forEach(key -> {
                ChunkUpload chunkUpload = trashFileUploadMap.get(key);
                if (chunkUpload.cleanServerUpload()) {
                    keys.add(key);
                }
            });

            keys.forEach(key -> {
                trashFileUploadMap.remove(key);
            });
        }

        return ResponseEntity.ok(status);
    }

    @RequestMapping(value = "cancel", method = RequestMethod.POST)
    public synchronized ResponseEntity<?> cancelUpload(
            String id,
            @ModelAttribute("appUser") AppUser appUser) {
        if (fileUploadMap.containsKey(id)) {
            ChunkUpload chunkUpload = fileUploadMap.remove(id);
            chunkUpload.stop();
            chunkUpload.resume();

            trashFileUploadMap.put(id, chunkUpload);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "status", method = RequestMethod.GET)
    public synchronized ResponseEntity<?> getUploadStatus(
            @RequestParam("id") String id,
            @ModelAttribute("appUser") AppUser appUser) {
        ChunkUpload chunkUpload = fileUploadMap.get(id);
        if (chunkUpload == null) {
            return ResponseEntity.ok(100);
        } else {
            return ResponseEntity.ok((int) (chunkUpload.progress * 100));
        }
    }

    @RequestMapping(value = "pause", method = RequestMethod.POST)
    public synchronized ResponseEntity<?> pauseUpload(
            String id,
            @ModelAttribute("appUser") AppUser appUser) {
        ChunkUpload chunkUpload = fileUploadMap.get(id);
        if (chunkUpload == null) {
            return ResponseEntity.notFound().build();
        } else {
            chunkUpload.suspend();
            return ResponseEntity.ok().build();
        }
    }

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public synchronized ResponseEntity<?> startUpload(
            DatasetUploadRequest uploadRequest,
            @ModelAttribute("appUser") AppUser appUser) {
        String id = uploadRequest.getId();
        String fileName = uploadRequest.getFileName();

        ChunkUpload chunkUpload = fileUploadMap.get(id);
        if (chunkUpload == null) {
            Path file = Paths.get(appUser.getDataDirectory(), fileName);
            if (Files.exists(file)) {
                UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
                String accountId = userAccount.getAccountId();
                String privateKey = userAccount.getPrivateKey();

                if (accountId == null || privateKey == null) {
                    return ResponseEntity.notFound().build();
                } else {
                    long chunkSize = 512 * 1024;
                    chunkUpload = new ChunkUpload(uploadRequest.getId(), file, chunkSize, appUser.getUsername(), dataUrl, restTemplate, userAccountService);
                    fileUploadMap.put(uploadRequest.getId(), chunkUpload);
                    executorService.execute(chunkUpload);
                    return ResponseEntity.ok().build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            if (chunkUpload.isSuspended()) {
                chunkUpload.resume();
            }
            return ResponseEntity.ok().build();
        }
    }

    public class UploadStatus {

        private String id;

        private boolean paused;

        public UploadStatus() {
        }

        public UploadStatus(String id, boolean paused) {
            this.id = id;
            this.paused = paused;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

    }

    private class ChunkUpload implements Runnable, RestRequestService {

        private final String id;

        private final Path file;

        private final long chunkSize;

        private final String username;

        private final String dataUrl;

        private final RestTemplate restTemplate;

        private final UserAccountService userAccountService;

        private double progress;

        private boolean suspended;

        private boolean stopped;

        private String resumableIdentifier;

        public ChunkUpload(String id, Path file, long chunkSize, String username, String dataUrl, RestTemplate restTemplate, UserAccountService userAccountService) {
            this.id = id;
            this.file = file;
            this.chunkSize = chunkSize;
            this.username = username;
            this.dataUrl = dataUrl;
            this.restTemplate = restTemplate;
            this.userAccountService = userAccountService;
        }

        @Override
        public void run() {
            progress = 0;
            suspended = false;
            stopped = false;
            try {
                String fileName = this.file.getFileName().toString();
                long fileSize = Files.size(this.file);
                long maxOffset = Math.max(Math.round(fileSize / this.chunkSize), 1);
                long resumableChunkSize = this.chunkSize;
                long resumableTotalSize = fileSize;

                long resumableTotalChunks = maxOffset;
                this.resumableIdentifier = String.format("%d-%s", fileSize, fileName.replaceAll("/[^0-9a-zA-Z_-]/img", ""));
                String resumableFilename = fileName;
                String resumableRelativePath = fileName;
                String resumableType = Files.probeContentType(this.file);

                try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file))) {
                    try {
                        for (long offset = 0; offset < maxOffset; offset++) {
                            UserAccount userAccount = userAccountService.findByUsername(username);
                            String accountId = userAccount.getAccountId();
                            String privateKey = userAccount.getPrivateKey();

                            suspended = suspended || accountId == null;

                            synchronized (this) {
                                while (suspended) {
                                    wait();
                                    if (stopped) {
                                        break;
                                    } else {
                                        userAccount = userAccountService.findByUsername(username);
                                        accountId = userAccount.getAccountId();
                                        suspended = suspended || accountId == null;
                                    }
                                }
                                if (stopped) {
                                    break;
                                }
                            }

                            long startByte = offset * this.chunkSize;
                            long endByte = Math.min(fileSize, (offset + 1) * this.chunkSize);
                            if (fileSize - endByte < this.chunkSize) {
                                endByte = fileSize;
                            }
                            long resumableChunkNumber = offset + 1;
                            long resumableCurrentChunkSize = endByte - startByte;

                            HttpStatus httpStatus;
                            try {
                                URI uri = UriComponentsBuilder.fromHttpUrl(this.dataUrl)
                                        .pathSegment("upload", "chunk")
                                        .queryParam("resumableIdentifier", this.resumableIdentifier)
                                        .queryParam("resumableChunkNumber", Long.toString(resumableChunkNumber))
                                        .queryParam("resumableChunkSize", Long.toString(resumableChunkSize))
                                        .buildAndExpand(accountId)
                                        .toUri();

                                String signature = WebSecurityDSA.createSignature(uri.toString(), privateKey);

                                HttpHeaders headers = new HttpHeaders();
                                headers.set(HEADER_ACCOUNT, accountId);
                                headers.set(HEADER_SIGNATURE, signature);

                                HttpEntity<Map<String, String>> entity = new HttpEntity(headers);
                                ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                                httpStatus = responseEntity.getStatusCode();
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
                                valueMap.add("resumableIdentifier", this.resumableIdentifier);
                                valueMap.add("resumableRelativePath", resumableRelativePath);
                                valueMap.add("resumableTotalChunks", resumableTotalChunks);
                                valueMap.add("resumableTotalSize", resumableTotalSize);
                                valueMap.add("resumableType", resumableType);

                                URI uri = UriComponentsBuilder.fromHttpUrl(this.dataUrl)
                                        .pathSegment("upload", "chunk")
                                        .buildAndExpand(accountId)
                                        .toUri();

                                String signature = WebSecurityDSA.createSignature(uri.toString(), privateKey);

                                HttpHeaders headers = new HttpHeaders();
                                headers.set(HEADER_ACCOUNT, accountId);
                                headers.set(HEADER_SIGNATURE, signature);

                                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(valueMap, headers);
                                try {
                                    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
                                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
                                        progress = (offset + 1.0) / maxOffset;
                                    }
                                } catch (HttpClientErrorException exception) {
                                    exception.printStackTrace(System.err);
                                }
                            }
                        }

                        if (stopped) {

                        }
                    } catch (InterruptedException exception) {
                        LOGGER.error(exception.getMessage());
                    }
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage());
                }
                fileUploadMap.remove(this.id);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        public boolean cleanServerUpload() {
            boolean flag = false;

            UserAccount userAccount = userAccountService.findByUsername(username);
            String accountId = userAccount.getAccountId();
            String privateKey = userAccount.getPrivateKey();

            if (accountId != null) {
                URI uri = UriComponentsBuilder.fromHttpUrl(this.dataUrl)
                        .pathSegment("upload", "chunk", "clean")
                        .buildAndExpand(accountId)
                        .toUri();

                String signature = WebSecurityDSA.createSignature(uri.toString(), privateKey);

                HttpHeaders headers = new HttpHeaders();
                headers.set(HEADER_ACCOUNT, accountId);
                headers.set(HEADER_SIGNATURE, signature);

                HttpEntity<CleanUploadRequest> entity = new HttpEntity(new CleanUploadRequest(this.resumableIdentifier), headers);
                try {
                    restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
                    flag = true;
                } catch (HttpClientErrorException exception) {
                    exception.printStackTrace(System.err);
                }
            }

            return flag;
        }

        public boolean isSuspended() {
            return suspended;
        }

        public synchronized void suspend() {
            this.suspended = true;
        }

        synchronized void resume() {
            suspended = false;
            notify();
        }

        public boolean isStopped() {
            return stopped;
        }

        public void stop() {
            this.stopped = true;
        }

        public double getProgress() {
            return progress;
        }

    }

    public static class CleanUploadRequest {

        private final String resumableIdentifier;

        public CleanUploadRequest(String resumableIdentifier) {
            this.resumableIdentifier = resumableIdentifier;
        }

        public String getResumableIdentifier() {
            return resumableIdentifier;
        }
    }

}
