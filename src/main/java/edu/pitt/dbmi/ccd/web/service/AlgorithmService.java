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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.cloud.dto.JobRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Apr 16, 2015 11:41:02 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgorithmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmService.class);

    private final UserAccountService userAccountService;

    private final JobQueueInfoService jobQueueInfoService;

    private final String appId;

    private final String userAlgorithmJobUri;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public AlgorithmService(
            UserAccountService userAccountService,
            JobQueueInfoService jobQueueInfoService,
            @Value("${ccd.rest.appId:1}") String appId,
            @Value("${ccd.job.algorithm.uri:http://localhost:9000/ccd-ws/job/algorithm}") String userAlgorithmJobUri,
            RestTemplate restTemplate) {
        this.userAccountService = userAccountService;
        this.jobQueueInfoService = jobQueueInfoService;
        this.appId = appId;
        this.userAlgorithmJobUri = userAlgorithmJobUri;
        this.restTemplate = restTemplate;
    }

    public void runRemotely(JobRequest jobRequest, AppUser appUser) {
        String uri = String.format("%s/submit?usr=%s&appId=%s", userAlgorithmJobUri, appUser.getUsername(), appId);
        try {
            restTemplate.postForEntity(uri, jobRequest, null);
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void runLocally(String algorithm, String algorithmJar, JobRequest jobRequest, AppUser appUser) {
        String userDataDir = appUser.getDataDirectory();
        String userTempDir = appUser.getTmpDirectory();
        String userOutputDir = appUser.getAlgoResultDir();
        String userLibDir = appUser.getLibDirectory();

        String algoName = jobRequest.getAlgorName();
        String dataset = jobRequest.getDataset();

        List<String> commands = new LinkedList<>();
        commands.add("java");

        String[] jvmOptions = jobRequest.getJvmOptions();
        if (jvmOptions != null) {
            commands.addAll(Arrays.asList(jvmOptions));
        }

        Path classPath = Paths.get(userLibDir, algorithmJar);
        commands.add("-cp");
        commands.add(classPath.toString());

        commands.add(algorithm);

        Path datasetPath = Paths.get(userDataDir, dataset);
        commands.add("--data");
        commands.add(datasetPath.toString());

        commands.addAll(Arrays.asList(jobRequest.getAlgoParams()));

        String fileName = String.format("%s_%s_%d", algoName, dataset, System.currentTimeMillis());
        commands.add("--out-filename");
        commands.add(fileName);

        StringBuilder buf = new StringBuilder();
        commands.forEach(cmd -> {
            buf.append(cmd);
            buf.append(";");
        });
        buf.deleteCharAt(buf.length() - 1);

        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algoName);
        jobQueueInfo.setCommands(buf.toString());
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(userOutputDir);
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(userTempDir);
        jobQueueInfo.setUserAccounts(Collections.singleton(userAccount));

        jobQueueInfo = jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);
        LOGGER.info("Add Job into Queue: " + jobQueueInfo.getId());
    }

}
