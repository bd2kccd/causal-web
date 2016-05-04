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
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Nov 17, 2015 11:46:21 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractAlgorithmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAlgorithmService.class);

    protected final String workspace;
    protected final String dataFolder;
    protected final String resultFolder;
    protected final String libFolder;
    protected final String tmpFolder;

    protected final String algorithmResultFolder;

    protected final DataService dataService;
    protected final DataFileService dataFileService;
    protected final VariableTypeService variableTypeService;
    protected final UserAccountService userAccountService;
    protected final JobQueueInfoService jobQueueInfoService;

    public AbstractAlgorithmService(
            String workspace,
            String dataFolder,
            String resultFolder,
            String libFolder,
            String tmpFolder,
            String algorithmResultFolder,
            DataService dataService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            UserAccountService userAccountService,
            JobQueueInfoService jobQueueInfoService) {
        this.workspace = workspace;
        this.dataFolder = dataFolder;
        this.resultFolder = resultFolder;
        this.libFolder = libFolder;
        this.tmpFolder = tmpFolder;
        this.algorithmResultFolder = algorithmResultFolder;
        this.dataService = dataService;
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.userAccountService = userAccountService;
        this.jobQueueInfoService = jobQueueInfoService;
    }

    public Map<String, String> getUserDataset(String username) {
        VariableType variableType = variableTypeService.findByName("continuous");

        return dataService.listAlgorithmDataset(username, variableType);
    }

    public String getFileDelimiter(String fileName, String username) {
        return dataService.getFileDelimiter(fileName, username);
    }

    public Long addToQueue(AlgorithmJobRequest jobRequest, String username) {
        String algorithmName = jobRequest.getAlgorithmName();
        String algorithmJar = jobRequest.getAlgorithmJar();
        String algorithm = jobRequest.getAlgorithm();
        List<String> dataset = jobRequest.getDataset();
        List<String> jvmOptions = jobRequest.getJvmOptions();
        List<String> parameters = jobRequest.getParameters();

        Path userResultDir = Paths.get(workspace, username, resultFolder, algorithmResultFolder);
        Path userTempDir = Paths.get(workspace, username, tmpFolder);

        List<String> commands = new LinkedList<>();
        commands.add("java");

        // add jvm options
        commands.addAll(jvmOptions);

        // add classpath
        Path classPath = Paths.get(workspace, libFolder, algorithmJar);
        commands.add("-jar");
        commands.add(classPath.toString());

        // add algorithm
        commands.add("--algorithm");
        commands.add(algorithm);

        // add dataset
        List<String> datasetPath = new LinkedList<>();
        dataset.forEach(dataFile -> {
            Path dataPath = Paths.get(workspace, username, dataFolder, dataFile);
            datasetPath.add(dataPath.toAbsolutePath().toString());
        });
        String datasetList = listToSeperatedValues(datasetPath, ",");
        commands.add("--data");
        commands.add(datasetList);

        // add parameters
        commands.addAll(parameters);

        // don't create any validation files
        commands.add("--no-validation-output");

        long currentTime = System.currentTimeMillis();
        String fileName = (dataset.size() > 1)
                ? String.format("%s_%s_%d", algorithmName, "multi-dataset", currentTime)
                : String.format("%s_%s_%d", algorithmName, listToSeperatedValues(dataset, ","), currentTime);
        commands.add("--output-prefix");
        commands.add(fileName);

        String cmd = listToSeperatedValues(commands, ";");

        UserAccount userAccount = userAccountService.findByUsername(username).get();
        JobQueueInfo jobQueueInfo = new JobQueueInfo();
        jobQueueInfo.setAddedTime(new Date(System.currentTimeMillis()));
        jobQueueInfo.setAlgorName(algorithmName);
        jobQueueInfo.setCommands(cmd);
        jobQueueInfo.setFileName(fileName);
        jobQueueInfo.setOutputDirectory(userResultDir.toAbsolutePath().toString());
        jobQueueInfo.setStatus(0);
        jobQueueInfo.setTmpDirectory(userTempDir.toAbsolutePath().toString());
        jobQueueInfo.setUserAccounts(Collections.singleton(userAccount));

        jobQueueInfo = jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);

        return jobQueueInfo.getId();
    }

    public String listToSeperatedValues(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        list.forEach(item -> {
            sb.append(item);
            sb.append(delimiter);
        });
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

}
