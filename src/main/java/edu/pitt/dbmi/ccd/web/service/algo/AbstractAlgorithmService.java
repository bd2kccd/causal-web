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
package edu.pitt.dbmi.ccd.web.service.algo;

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.DataService;
import edu.pitt.dbmi.ccd.web.service.cloud.dto.AlgorithmJobRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Sep 26, 2015 7:56:24 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractAlgorithmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAlgorithmService.class);

    protected final DataService dataService;

    protected final DataFileService dataFileService;

    protected final VariableTypeService variableTypeService;

    protected final UserAccountService userAccountService;

    protected final JobQueueInfoService jobQueueInfoService;

    public AbstractAlgorithmService(
            DataService dataService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            UserAccountService userAccountService,
            JobQueueInfoService jobQueueInfoService) {
        this.dataService = dataService;
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.userAccountService = userAccountService;
        this.jobQueueInfoService = jobQueueInfoService;
    }

    protected Map<String, String> getUserDataFile(String prefix, String username) {
        VariableType variableType = variableTypeService.findByName("continuous");

        return dataService.listAlgoDataset(prefix, username, variableType);
    }

    protected Map<String, String> getUserDataFile(String username) {
        VariableType variableType = variableTypeService.findByName("continuous");

        return dataService.listAlgoDataset(username, variableType);
    }

    public String getFileDelimiter(String absolutePath, String fileName) {
        return dataService.getFileDelimiter(absolutePath, fileName);
    }

    protected Long addToLocalQueue(String algorithmJar, AlgorithmJobRequest jobRequest, AppUser appUser) {
        String userDataDir = appUser.getDataDirectory();
        String userTempDir = appUser.getTmpDirectory();
        String userOutputDir = appUser.getAlgoResultDir();
        String userLibDir = appUser.getLibDirectory();

        String algorithmName = jobRequest.getAlgorithmName();
        String algorithm = jobRequest.getAlgorithm();
        String dataset = jobRequest.getDataset();
        String[] jvmOptions = jobRequest.getJvmOptions();
        String[] parameters = jobRequest.getParameters();

        List<String> commands = new LinkedList<>();
        commands.add("java");

        if (jvmOptions != null) {
            commands.addAll(Arrays.asList(jvmOptions));
        }

        Path classPath = Paths.get(userLibDir, algorithmJar);
        commands.add("-cp");
        commands.add(classPath.toString());

        commands.add(algorithm);

        Path datasetPath;
        if (dataset == null) {
            commands.add("--data-dir");
            datasetPath = Paths.get(userDataDir);
        } else {
            datasetPath = Paths.get(userDataDir, dataset);
            commands.add("--data");
        }
        commands.add(datasetPath.toString());

        commands.addAll(Arrays.asList(parameters));

        String fileName = String.format("%s_%s_%d", algorithmName, (dataset == null) ? "multi" : dataset, System.currentTimeMillis());
        commands.add("--out-filename");
        commands.add(fileName);

        StringBuilder buf = new StringBuilder();
        commands.forEach(cmd -> {
            buf.append(cmd);
            buf.append(";");
        });
        buf.deleteCharAt(buf.length() - 1);

        String cmd = buf.toString();
        LOGGER.info(cmd);

        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algorithmName);
        jobQueueInfo.setCommands(cmd);
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(userOutputDir);
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(userTempDir);
        jobQueueInfo.setUserAccounts(Collections.singleton(userAccount));

        jobQueueInfo = jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);

        return jobQueueInfo.getId();
    }

}
