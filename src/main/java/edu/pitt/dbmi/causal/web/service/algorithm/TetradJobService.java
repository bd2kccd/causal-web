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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobRunService;
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
public class TetradJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TetradJobService.class);

    private final JobRunService jobRunService;

    @Autowired
    public TetradJobService(JobRunService jobRunService) {
        this.jobRunService = jobRunService;
    }

    public void submitJob(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, UserAccount userAccount) {
        String name = tetradForm.getName();
        String cmdParams = buildCmdParameters(tetradForm, multiValueMap, userAccount);

        jobRunService.submitTetradJobRun(name, tetradForm.getDescription(), cmdParams, userAccount);
    }

    private String buildCmdParameters(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, UserAccount userAccount) {
        List<String> cmdParamList = new LinkedList<>();
        extractFileParameters(tetradForm, cmdParamList);
        extractMiscParameters(tetradForm, cmdParamList);
        extractTetradParameters(tetradForm, multiValueMap, cmdParamList);

        return cmdParamList.stream().collect(Collectors.joining("|"));
    }

    private void extractMiscParameters(TetradForm tetradForm, List<String> cmdParamList) {
        int jvmMaxMem = tetradForm.getJvmMaxMem();
        if (jvmMaxMem > 0) {
            cmdParamList.add("jvmMaxMem:" + jvmMaxMem);
        }
    }

    private void extractFileParameters(TetradForm tetradForm, List<String> cmdParamList) {
        Long dataFileId = tetradForm.getDataFileId();
        Long knwlFileId = tetradForm.getKnwlFileId();
        Long varFileId = tetradForm.getVarFileId();
        boolean singleDataFile = tetradForm.isSingleDataFile();

        cmdParamList.add("dataFile:" + dataFileId);
        cmdParamList.add("singleDataFile:" + singleDataFile);
        if (knwlFileId != null) {
            cmdParamList.add("knwlFile:" + knwlFileId);
        }
        if (varFileId != null) {
            cmdParamList.add("varFile:" + varFileId);
        }
    }

    private void extractTetradParameters(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, List<String> cmdParamList) {
        String algorithmName = tetradForm.getAlgorithm();
        String scoreName = tetradForm.getScore();
        String testName = tetradForm.getTest();

        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algorithmName);
        TetradScore score = TetradScores.getInstance().getTetradScore(scoreName);
        TetradTest test = TetradTests.getInstance().getTetradTest(testName);

        // add algorithm, test, and score
        cmdParamList.add(String.format("algorithm:%s", algorithm.getAlgorithm().getAnnotation().command()));
        if (score != null) {
            cmdParamList.add(String.format("score:%s", score.getScore().getAnnotation().command()));
        }
        if (test != null) {
            cmdParamList.add(String.format("test:%s", test.getTest().getAnnotation().command()));
        }

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
    }

}
