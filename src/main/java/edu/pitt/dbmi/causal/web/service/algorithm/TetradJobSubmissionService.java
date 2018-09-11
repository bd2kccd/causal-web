/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.service.algorithm;

import edu.pitt.dbmi.causal.web.model.ParamOption;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradParams;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.ccd.db.code.AlgorithmTypeCodes;
import edu.pitt.dbmi.ccd.db.code.JobStatusCodes;
import edu.pitt.dbmi.ccd.db.entity.AlgorithmType;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.JobDetail;
import edu.pitt.dbmi.ccd.db.entity.JobStatus;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.TetradJob;
import edu.pitt.dbmi.ccd.db.entity.TetradVariableFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.AlgorithmTypeService;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.JobRunService;
import edu.pitt.dbmi.ccd.db.service.JobStatusService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 *
 * Mar 5, 2018 2:24:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class TetradJobSubmissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TetradJobSubmissionService.class);

    private final FileService fileService;
    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;
    private final JobStatusService jobStatusService;
    private final AlgorithmTypeService algorithmTypeService;
    private final JobRunService jobRunService;

    @Autowired
    public TetradJobSubmissionService(FileService fileService, FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService, JobStatusService jobStatusService, AlgorithmTypeService algorithmTypeService, JobRunService jobRunService) {
        this.fileService = fileService;
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
        this.jobStatusService = jobStatusService;
        this.algorithmTypeService = algorithmTypeService;
        this.jobRunService = jobRunService;
    }

    public void submitJob(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, UserAccount userAccount) {
        JobDetail jobDetail = createJobDetail(tetradForm, multiValueMap, userAccount);
        TetradJob tetradJob = createTetradJob(tetradForm, multiValueMap, userAccount);

        jobRunService.submitTetradJobRun(jobDetail, tetradJob, userAccount);
    }

    private TetradJob createTetradJob(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, UserAccount userAccount) {
        Long dataFileId = tetradForm.getDataFileId();
        Long varFileId = tetradForm.getVarFileId();
        Long knwlFileId = tetradForm.getKnwlFileId();
        boolean isSingleFile = tetradForm.isSingleDataFile();
        int jvmMaxMem = tetradForm.getJvmMaxMem();

        TetradDataFile tetradDataFile = isSingleFile
                ? tetradDataFileService.getRepository().findByIdAndUserAccount(dataFileId, userAccount)
                : null;

        FileGroup fileGroup = isSingleFile
                ? null
                : fileGroupService.getRepository().findByIdAndUserAccount(dataFileId, userAccount);

        TetradVariableFile tetradVariableFile = (knwlFileId == null)
                ? null
                : tetradVariableFileService.getRepository().findByIdAndUserAccount(varFileId, userAccount);

        File knowledgeFile = (knwlFileId == null)
                ? null
                : fileService.getRepository().findByIdAndUserAccount(knwlFileId, userAccount);

        String jvmParameter = (jvmMaxMem > 0) ? "jvmMaxMem:" + jvmMaxMem : null;

        String algorithm = createAlgorithm(tetradForm, multiValueMap);

        String algorithmParameter = createAlgorithmParameters(tetradForm, multiValueMap);

        TetradJob tetradJob = new TetradJob();
        tetradJob.setTetradDataFile(tetradDataFile);
        tetradJob.setFileGroup(fileGroup);
        tetradJob.setTetradVariableFile(tetradVariableFile);
        tetradJob.setKnowledgeFile(knowledgeFile);
        tetradJob.setJvmParameter(jvmParameter);
        tetradJob.setAlgorithm(algorithm);
        tetradJob.setAlgorithmParameter(algorithmParameter);

        return tetradJob;
    }

    private JobDetail createJobDetail(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, UserAccount userAccount) {
        String name = tetradForm.getName();
        String description = tetradForm.getDescription();
        Date creationTime = new Date();
        JobStatus jobStatus = jobStatusService.getRepository().findByCode(JobStatusCodes.QUEUED);
        AlgorithmType algorithmType = algorithmTypeService.getRepository().findByCode(AlgorithmTypeCodes.TETRAD);

        JobDetail jobDetail = new JobDetail();
        jobDetail.setName(name);
        jobDetail.setDescription(description);
        jobDetail.setCreationTime(creationTime);
        jobDetail.setJobStatus(jobStatus);
        jobDetail.setAlgorithmType(algorithmType);
        jobDetail.setUserAccount(userAccount);

        return jobDetail;
    }

    private String createAlgorithm(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap) {
        List<String> cmdAlgoList = new LinkedList<>();

        String algorithmName = tetradForm.getAlgorithm();
        String scoreName = tetradForm.getScore();
        String testName = tetradForm.getTest();

        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algorithmName);
        TetradScore score = TetradScores.getInstance().getTetradScore(scoreName);
        TetradTest test = TetradTests.getInstance().getTetradTest(testName);

        // add algorithm, test, and score
        cmdAlgoList.add(String.format("algorithm:%s", algorithm.getAlgorithm().getAnnotation().command()));
        if (score != null) {
            cmdAlgoList.add(String.format("score:%s", score.getScore().getAnnotation().command()));
        }
        if (test != null) {
            cmdAlgoList.add(String.format("test:%s", test.getTest().getAnnotation().command()));
        }

        return cmdAlgoList.stream().collect(Collectors.joining("|"));
    }

    private String createAlgorithmParameters(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap) {
        List<String> cmdParamList = new LinkedList<>();

        String algorithmName = tetradForm.getAlgorithm();
        String scoreName = tetradForm.getScore();
        String testName = tetradForm.getTest();

        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algorithmName);
        TetradScore score = TetradScores.getInstance().getTetradScore(scoreName);
        TetradTest test = TetradTests.getInstance().getTetradTest(testName);

        // add parameters
        List<ParamOption> paramOpts = TetradParams.getInstance().getParamOptions(algorithm, score, test);
        paramOpts.forEach(paramOpt -> {
            String param = paramOpt.getValue();
            if (paramOpt.isaBoolean()) {
                if (multiValueMap.containsKey(param)) {
                    cmdParamList.add(param + ":true");
                } else {
                    cmdParamList.add(param + ":false");
                }
            } else {
                String val = multiValueMap.getFirst(param);
                if (val == null) {
                    val = paramOpt.getDefaultVal();
                }

                cmdParamList.add(String.format("%s:%s", param, val));
            }
        });

        return cmdParamList.stream().collect(Collectors.joining("|"));
    }

}
