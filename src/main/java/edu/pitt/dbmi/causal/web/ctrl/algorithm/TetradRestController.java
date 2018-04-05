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
package edu.pitt.dbmi.causal.web.ctrl.algorithm;

import edu.cmu.tetrad.annotation.AlgType;
import edu.cmu.tetrad.data.DataType;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.OptionModel;
import edu.pitt.dbmi.causal.web.model.ParamOption;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.algorithm.TetradService;
import edu.pitt.dbmi.causal.web.tetrad.AlgoTypes;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.ccd.db.domain.file.FileGroupListItem;
import edu.pitt.dbmi.ccd.db.domain.file.TetradDataListItem;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Mar 6, 2018 3:24:20 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/tetrad")
public class TetradRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TetradRestController.class);

    private static final OptionModel EMPTY_OPTION_MODEL = new OptionModel(Collections.EMPTY_LIST, "");

    private final AppUserService appUserService;
    private final TetradService tetradService;
    private final TetradDataFileService tetradDataFileService;
    private final FileGroupService fileGroupService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public TetradRestController(AppUserService appUserService, TetradService tetradService, TetradDataFileService tetradDataFileService, FileGroupService fileGroupService, VariableTypeService variableTypeService) {
        this.appUserService = appUserService;
        this.tetradService = tetradService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileGroupService = fileGroupService;
        this.variableTypeService = variableTypeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "data/multiple/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listMultipleData(@PathVariable final Long varTypeId, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        Optional<VariableType> varType = variableTypeService.findById(varTypeId);
        if (!varType.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<FileGroupListItem> listFiles = fileGroupService.getRepository()
                .getFileGroupListItems(userAccount, varType.get());

        return ResponseEntity.ok(listFiles);
    }

    @RequestMapping(value = "data/single/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listSingleData(@PathVariable final Long varTypeId, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        Optional<VariableType> varType = variableTypeService.findById(varTypeId);
        if (!varType.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<TetradDataListItem> listFiles = tetradDataFileService.getRepository()
                .getTetradDataListFiles(userAccount, varType.get());

        return ResponseEntity.ok(listFiles);
    }

    @RequestMapping(value = "algo/{algoTypeName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listAlgorithms(@PathVariable final String algoTypeName) {
        if ("all".equals(algoTypeName)) {
            return ResponseEntity.ok(TetradAlgorithms.getInstance().getOptions());
        } else {
            AlgType algType = AlgoTypes.getInstance().getAlgType(algoTypeName);
            if (algType == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(TetradAlgorithms.getInstance().getOptions(algType));
        }
    }

    @RequestMapping(value = "score/algo/{algoName}/varType/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listScores(
            @PathVariable final String algoName,
            @PathVariable final Long varTypeId) {
        TetradAlgorithm algoOpt = TetradAlgorithms.getInstance().getTetradAlgorithm(algoName);
        if (algoOpt == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(String.format("No such algorithm %s.", algoName));
        }

        Optional<VariableType> varType = variableTypeService.findById(varTypeId);
        if (!varType.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No such variable type.");
        }

        if (algoOpt.isRequiredScore()) {
            switch (varType.get().getShortName()) {
                case VariableTypeService.CONTINUOUS_SHORT_NAME:
                    return ResponseEntity.ok(TetradScores.getInstance().getOptionModel(DataType.Continuous));
                case VariableTypeService.DISCRETE_SHORT_NAME:
                    return ResponseEntity.ok(TetradScores.getInstance().getOptionModel(DataType.Discrete));
                case VariableTypeService.MIXED_SHORT_NAME:
                    return ResponseEntity.ok(TetradScores.getInstance().getOptionModel(DataType.Mixed));
                default:
                    return ResponseEntity.ok(EMPTY_OPTION_MODEL);
            }
        } else {
            return ResponseEntity.ok(EMPTY_OPTION_MODEL);
        }
    }

    @RequestMapping(value = "test/algo/{algoName}/varType/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listTests(
            @PathVariable final String algoName,
            @PathVariable final Long varTypeId) {
        TetradAlgorithm algoOpt = TetradAlgorithms.getInstance().getTetradAlgorithm(algoName);
        if (algoOpt == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<VariableType> varType = variableTypeService.findById(varTypeId);
        if (!varType.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        if (algoOpt.isRequiredTest()) {
            switch (varType.get().getShortName()) {
                case VariableTypeService.CONTINUOUS_SHORT_NAME:
                    return ResponseEntity.ok(TetradTests.getInstance().getOptionModel(DataType.Continuous));
                case VariableTypeService.DISCRETE_SHORT_NAME:
                    return ResponseEntity.ok(TetradTests.getInstance().getOptionModel(DataType.Discrete));
                case VariableTypeService.MIXED_SHORT_NAME:
                    return ResponseEntity.ok(TetradTests.getInstance().getOptionModel(DataType.Mixed));
                default:
                    return ResponseEntity.ok(EMPTY_OPTION_MODEL);
            }
        } else {
            return ResponseEntity.ok(EMPTY_OPTION_MODEL);
        }
    }

    @RequestMapping(value = "param/algo/{algoName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listParameters(
            @PathVariable final String algoName,
            @RequestParam("score") String scoreShortName,
            @RequestParam("test") String testShortName) {
        TetradAlgorithm tetradAlgo = TetradAlgorithms.getInstance().getTetradAlgorithm(algoName);
        if (tetradAlgo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such algorithm.");
        }

        Class score;
        if (scoreShortName == null) {
            score = null;
        } else {
            TetradScore tetradScore = TetradScores.getInstance().getTetradScore(scoreShortName);
            if (tetradScore == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such independence test.");
            }
            score = tetradScore.getScore().getClazz();
        }

        Class test;
        if (testShortName == null) {
            test = null;
        } else {
            TetradTest tetradTest = TetradTests.getInstance().getTetradTest(testShortName);
            if (tetradTest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such score.");
            }
            test = tetradTest.getTest().getClazz();
        }

        StringBuilder errMsg = new StringBuilder();
        if (tetradService.validate(tetradAlgo, score, test, errMsg)) {
            List<ParamOption> params = tetradService
                    .getAlgorithmParameters(tetradAlgo.getAlgorithm().getClazz(), score, test);

            return ResponseEntity.ok(params);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errMsg.toString());
        }
    }

}
