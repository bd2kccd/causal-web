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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.AlgorithmFactory;
import edu.pitt.dbmi.causal.web.model.ParamOption;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.tetrad.AlgoTypes;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradParams;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
public class TetradService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TetradService.class);

    private final TetradDataFileService tetradDataFileService;
    private final FileGroupService fileGroupService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public TetradService(TetradDataFileService tetradDataFileService, FileGroupService fileGroupService, VariableTypeService variableTypeService) {
        this.tetradDataFileService = tetradDataFileService;
        this.fileGroupService = fileGroupService;
        this.variableTypeService = variableTypeService;
    }

    public TetradForm createTetradForm() {
        TetradForm tetradForm = new TetradForm();

        Optional<VariableType> varType = variableTypeService.findAll().stream()
                .findFirst();
        if (varType.isPresent()) {
            tetradForm.setVarTypeId(varType.get().getId());
        }

        tetradForm.setAlgoType(AlgoTypes.DEFAULT_VALUE);

        return tetradForm;
    }

    public void enqueueJob(TetradForm tetradForm, MultiValueMap<String, String> multiValueMap, UserAccount userAccount) throws Exception {
        Long datasetId = tetradForm.getDatasetId();
        boolean isSingleFile = tetradForm.isSingleFile();
        Long varTypeId = tetradForm.getVarTypeId();
        String algorithmName = tetradForm.getAlgorithm();
        String testName = tetradForm.getTest();
        String scoreName = tetradForm.getScore();

        Optional<VariableType> varTypeOpt = variableTypeService.getRepository().findById(varTypeId);
        VariableType variableType = varTypeOpt.isPresent() ? varTypeOpt.get() : null;

        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algorithmName);
        TetradScore score = TetradScores.getInstance().getTetradScore(scoreName);
        TetradTest test = TetradTests.getInstance().getTetradTest(testName);

        List<ParamOption> paramOpts = getAlgorithmParameters(algorithm, score, test);
        String params = buildCmdParameters(multiValueMap, paramOpts);
    }

    public String buildCmdParameters(MultiValueMap<String, String> multiValueMap, List<ParamOption> paramOpts) throws JsonProcessingException {
        Map<String, String> params = new HashMap<>();
        paramOpts.forEach(e -> {
            String param = e.getValue();
            String val = multiValueMap.getFirst(param);
            if (e.isaBoolean()) {
                if (val != null && (val.equals("on") || val.equals("yes") || val.equals("true"))) {
                    params.put(param, "true");
                }
            } else {
                params.put(param, val);
            }
        });

        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException exception) {
            LOGGER.error("Unable to build parameters as JSON object.", exception);

            throw exception;
        }
    }

    public List<ParamOption> getAlgorithmParameters(TetradAlgorithm algorithm, TetradScore score, TetradTest test) {
        Class scoreClass = (score == null) ? null : score.getScore().getClazz();
        Class testClass = (test == null) ? null : test.getTest().getClazz();
        Class algoClass = algorithm.getAlgorithm().getClazz();

        List<String> params = new LinkedList<>();
        try {
            Algorithm algo = AlgorithmFactory.create(algoClass, testClass, scoreClass);
            if (algo != null) {
                params.addAll(algo.getParameters());
            }
        } catch (IllegalAccessException | InstantiationException exception) {
            LOGGER.error("", exception);
        }

        TetradParams tetradParams = TetradParams.getInstance();

        List<ParamOption> numParams = new LinkedList<>();
        List<ParamOption> boolParams = new LinkedList<>();
        params.stream()
                .map(e -> tetradParams.getParamOption(e))
                .collect(Collectors.toList())
                .forEach(opt -> {
                    if (opt.isNumeric()) {
                        numParams.add(opt);
                    } else if (opt.isaBoolean()) {
                        boolParams.add(opt);
                    }
                });

        return Stream.concat(numParams.stream(), boolParams.stream())
                .collect(Collectors.toList());
    }

}
