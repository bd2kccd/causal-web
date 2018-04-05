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

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.AlgorithmFactory;
import edu.pitt.dbmi.causal.web.model.ParamOption;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.tetrad.AlgoTypes;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradParams;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<ParamOption> getAlgorithmParameters(Class algorithm, Class score, Class test) {
        List<String> params = new LinkedList<>();
        try {
            Algorithm algo = AlgorithmFactory.create(algorithm, test, score);
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

    public boolean validate(TetradAlgorithm tetradAlgorithm, Class score, Class test, StringBuilder errMsg) {
        boolean missingScore = tetradAlgorithm.isRequiredScore() && (score == null);
        boolean missingTest = tetradAlgorithm.isRequiredTest() && (test == null);
        if (missingTest || missingScore) {
            String algoName = tetradAlgorithm.getAlgorithm().getAnnotation().name();

            String msg;
            if (missingTest && missingScore) {
                msg = String.format("%s requires both test and score.", algoName);
            } else if (missingScore) {
                msg = String.format("%s requires test.", algoName);
            } else {
                msg = String.format("%s requires score.", algoName);
            }
            errMsg.append(msg);

            return false;
        }

        return true;
    }

}
