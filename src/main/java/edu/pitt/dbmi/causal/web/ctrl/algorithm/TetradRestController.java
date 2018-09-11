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
import edu.pitt.dbmi.causal.web.model.Option;
import edu.pitt.dbmi.causal.web.model.OptionModel;
import edu.pitt.dbmi.causal.web.model.ParamOption;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.algorithm.TetradJobSubmissionService;
import edu.pitt.dbmi.causal.web.tetrad.AlgoTypes;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradParams;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.ccd.db.code.FileFormatCodes;
import edu.pitt.dbmi.ccd.db.code.VariableTypeCodes;
import edu.pitt.dbmi.ccd.db.domain.file.FileGroupListItem;
import edu.pitt.dbmi.ccd.db.domain.file.FileListItem;
import edu.pitt.dbmi.ccd.db.domain.file.TetradDataListItem;
import edu.pitt.dbmi.ccd.db.domain.file.TetradVarListItem;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping(value = "secured/ws/algorithm/tetrad")
public class TetradRestController {

    private static final OptionModel EMPTY_OPTION_MODEL = new OptionModel(Collections.EMPTY_LIST, "");

    private static final String ALGORITHM_NOT_FOUND = "No such algorithm found.";
    private static final String VARTYPE_NOT_FOUND = "No such variable type found.";

    private final AppUserService appUserService;
    private final TetradJobSubmissionService tetradService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;
    private final FileGroupService fileGroupService;
    private final VariableTypeService variableTypeService;
    private final FileService fileService;
    private final FileFormatService fileFormatService;

    @Autowired
    public TetradRestController(AppUserService appUserService, TetradJobSubmissionService tetradService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService, FileGroupService fileGroupService, VariableTypeService variableTypeService, FileService fileService, FileFormatService fileFormatService) {
        this.appUserService = appUserService;
        this.tetradService = tetradService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
        this.fileGroupService = fileGroupService;
        this.variableTypeService = variableTypeService;
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping(value = "description/algo/{algoName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> getAlgorithmDescription(@PathVariable final String algoName) {
        TetradAlgorithm algo = TetradAlgorithms.getInstance().getTetradAlgorithm(algoName);

        return (algo == null)
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(ALGORITHM_NOT_FOUND)
                : ResponseEntity.ok(algo.getDescription());
    }

    @GetMapping(value = "param/algo/{algoName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listParameters(
            @PathVariable("algoName") final String algorithmName,
            @RequestParam("score") String scoreName,
            @RequestParam("test") String testName) {
        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algorithmName);
        if (algorithm == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ALGORITHM_NOT_FOUND);
        }

        TetradScore score = TetradScores.getInstance().getTetradScore(scoreName);
        TetradTest test = TetradTests.getInstance().getTetradTest(testName);

        List<ParamOption> opts = TetradParams.getInstance()
                .getParamOptions(algorithm, score, test);

        return ResponseEntity.ok(opts);
    }

    @GetMapping(value = "score/algo/{algoName}/varType/{varTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listScores(
            @PathVariable final String algoName,
            @PathVariable final long varTypeId) {
        TetradAlgorithm algoOpt = TetradAlgorithms.getInstance().getTetradAlgorithm(algoName);
        if (algoOpt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("No such algorithm %s.", algoName));
        }

        if (algoOpt.isRequiredScore()) {
            VariableType varType = variableTypeService.findById(varTypeId);
            if (varType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(VARTYPE_NOT_FOUND);
            }

            switch (varType.getCode()) {
                case VariableTypeCodes.CONTINUOUS:
                    return ResponseEntity.ok(TetradScores.getInstance().getOptionModel(DataType.Continuous));
                case VariableTypeCodes.DISCRETE:
                    return ResponseEntity.ok(TetradScores.getInstance().getOptionModel(DataType.Discrete));
                case VariableTypeCodes.MIXED:
                    return ResponseEntity.ok(TetradScores.getInstance().getOptionModel(DataType.Mixed));
            }
        }

        return ResponseEntity.ok(EMPTY_OPTION_MODEL);
    }

    @GetMapping(value = "test/algo/{algoName}/varType/{varTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listTests(
            @PathVariable final String algoName,
            @PathVariable final long varTypeId) {
        TetradAlgorithm algoOpt = TetradAlgorithms.getInstance().getTetradAlgorithm(algoName);
        if (algoOpt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("No such algorithm %s.", algoName));
        }

        if (algoOpt.isRequiredTest()) {
            VariableType varType = variableTypeService.findById(varTypeId);
            if (varType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(VARTYPE_NOT_FOUND);
            }

            switch (varType.getCode()) {
                case VariableTypeCodes.CONTINUOUS:
                    return ResponseEntity.ok(TetradTests.getInstance().getOptionModel(DataType.Continuous));
                case VariableTypeCodes.DISCRETE:
                    return ResponseEntity.ok(TetradTests.getInstance().getOptionModel(DataType.Discrete));
                case VariableTypeCodes.MIXED:
                    return ResponseEntity.ok(TetradTests.getInstance().getOptionModel(DataType.Mixed));
            }
        }

        return ResponseEntity.ok(EMPTY_OPTION_MODEL);
    }

    @GetMapping(value = "algo/{algoTypeName}/multiple", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listMultiDatasetAlgorithms(
            @PathVariable("algoTypeName") final String algoTypeName,
            @RequestParam("knowledge") final boolean acceptKnowledge) {
        TetradAlgorithms algorithms = TetradAlgorithms.getInstance();
        if ("all".equals(algoTypeName)) {
            List<Option> opts = acceptKnowledge
                    ? algorithms.getMultiDatasetOptionsAndknowledgeOptions()
                    : algorithms.getMultiDatasetOptions();

            return ResponseEntity.ok(opts);
        } else {
            AlgType algType = AlgoTypes.getInstance().getAlgType(algoTypeName);
            if (algType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such algorithm type.");
            }

            List<Option> opts = acceptKnowledge
                    ? algorithms.getMultiDatasetOptionsAndknowledgeOptions(algType)
                    : algorithms.getMultiDatasetOptions(algType);

            return ResponseEntity.ok(opts);
        }
    }

    @GetMapping(value = "algo/{algoTypeName}/single", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listSingleDatasetAlgorithms(
            @PathVariable("algoTypeName") final String algoTypeName,
            @RequestParam("knowledge") final boolean acceptKnowledge) {
        TetradAlgorithms algorithms = TetradAlgorithms.getInstance();
        if ("all".equals(algoTypeName)) {
            List<Option> opts = acceptKnowledge
                    ? algorithms.getKnowledgeOptions()
                    : algorithms.getOptions();

            return ResponseEntity.ok(opts);
        } else {
            AlgType algType = AlgoTypes.getInstance().getAlgType(algoTypeName);
            if (algType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such algorithm type.");
            }

            List<Option> opts = acceptKnowledge
                    ? algorithms.getKnowledgeOptions(algType)
                    : algorithms.getOptions(algType);

            return ResponseEntity.ok(opts);
        }
    }

    @GetMapping(value = "variable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listVariableFiles(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        List<TetradVarListItem> listFiles = tetradVariableFileService.getRepository()
                .getTetradVarListItems(userAccount);

        return ResponseEntity.ok(listFiles);
    }

    @GetMapping(value = "knowledge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listKnowledgeFiles(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        FileFormat fileFormat = fileFormatService.findByCode(FileFormatCodes.TETRAD_KNWL);
        List<FileListItem> listItems = fileService.getRepository()
                .getByFileFormat(fileFormat, userAccount);

        return ResponseEntity.ok(listItems);
    }

    @GetMapping(value = "data/varTypeId/{varTypeId}/multiple", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listMultipleDataFiles(@PathVariable final Long varTypeId, final AppUser appUser) {
        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(VARTYPE_NOT_FOUND);
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        List<FileGroupListItem> listFiles = fileGroupService.getRepository()
                .getFileGroupListItems(userAccount, varType);

        return ResponseEntity.ok(listFiles);
    }

    @GetMapping(value = "data/varTypeId/{varTypeId}/single", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listSingleDataFiles(@PathVariable final Long varTypeId, final AppUser appUser) {
        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(VARTYPE_NOT_FOUND);
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        List<TetradDataListItem> listFiles = tetradDataFileService.getRepository()
                .getTetradDataListItems(userAccount, varType);

        return ResponseEntity.ok(listFiles);
    }

}
