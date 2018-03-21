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
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.tetrad.AlgoTypes;
import edu.pitt.dbmi.causal.web.tetrad.AlgorithmOpt;
import edu.pitt.dbmi.causal.web.tetrad.AlgorithmOpts;
import edu.pitt.dbmi.causal.web.tetrad.ScoreOpts;
import edu.pitt.dbmi.causal.web.tetrad.TestOpts;
import edu.pitt.dbmi.ccd.db.domain.file.FileGroupListItem;
import edu.pitt.dbmi.ccd.db.domain.file.TetradDataListItem;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    private final AppUserService appUserService;
    private final TetradDataFileService tetradDataFileService;
    private final FileGroupService fileGroupService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public TetradRestController(AppUserService appUserService, TetradDataFileService tetradDataFileService, FileGroupService fileGroupService, VariableTypeService variableTypeService) {
        this.appUserService = appUserService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileGroupService = fileGroupService;
        this.variableTypeService = variableTypeService;
    }

    @RequestMapping(value = "data/multiple/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listMultipleData(@PathVariable final Long varTypeId, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            return ResponseEntity.notFound().build();
        }
        List<FileGroupListItem> listFiles = fileGroupService.getRepository()
                .getFileGroupListItems(userAccount, varType);

        return ResponseEntity.ok(listFiles);
    }

    @RequestMapping(value = "data/single/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listSingleData(@PathVariable final Long varTypeId, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            return ResponseEntity.notFound().build();
        }

        List<TetradDataListItem> listFiles = tetradDataFileService.getRepository()
                .getTetradDataListFiles(userAccount, varType);

        return ResponseEntity.ok(listFiles);
    }

    @RequestMapping(value = "algo/{algoTypeName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listAlgorithms(@PathVariable final String algoTypeName) {
        if ("all".equals(algoTypeName)) {
            return ResponseEntity.ok(AlgorithmOpts.getInstance().getAllOptions());
        } else {
            AlgType algType = AlgoTypes.getInstance().getAlgType(algoTypeName);
            if (algType == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(AlgorithmOpts.getInstance().getOptions(algType));
        }
    }

    @RequestMapping(value = "test/algo/{algoName}/varType/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listTests(
            @PathVariable final String algoName,
            @PathVariable final Long varTypeId) {
        AlgorithmOpt algoOpt = AlgorithmOpts.getInstance().getAlgorithmOpt(algoName);
        if (algoOpt == null) {
            return ResponseEntity.notFound().build();
        }

        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            return ResponseEntity.notFound().build();
        }

        if (algoOpt.isRequiredTest()) {
            switch (varType.getShortName()) {
                case VariableTypeService.CONTINUOUS_SHORT_NAME:
                    return ResponseEntity.ok(TestOpts.getInstance().getOptions().get(DataType.Continuous));
                case VariableTypeService.DISCRETE_SHORT_NAME:
                    return ResponseEntity.ok(TestOpts.getInstance().getOptions().get(DataType.Discrete));
                case VariableTypeService.MIXED_SHORT_NAME:
                    return ResponseEntity.ok(TestOpts.getInstance().getOptions().get(DataType.Mixed));
                default:
                    return ResponseEntity.ok(Collections.EMPTY_LIST);
            }
        } else {
            return ResponseEntity.ok(Collections.EMPTY_LIST);
        }
    }

    @RequestMapping(value = "score/algo/{algoName}/varType/{varTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listScores(
            @PathVariable final String algoName,
            @PathVariable final Long varTypeId) {
        AlgorithmOpt algoOpt = AlgorithmOpts.getInstance().getAlgorithmOpt(algoName);
        if (algoOpt == null) {
            return ResponseEntity.notFound().build();
        }

        VariableType varType = variableTypeService.findById(varTypeId);
        if (varType == null) {
            return ResponseEntity.notFound().build();
        }

        if (algoOpt.isRequiredScore()) {
            switch (varType.getShortName()) {
                case VariableTypeService.CONTINUOUS_SHORT_NAME:
                    return ResponseEntity.ok(ScoreOpts.getInstance().getOptions().get(DataType.Continuous));
                case VariableTypeService.DISCRETE_SHORT_NAME:
                    return ResponseEntity.ok(ScoreOpts.getInstance().getOptions().get(DataType.Discrete));
                case VariableTypeService.MIXED_SHORT_NAME:
                    return ResponseEntity.ok(ScoreOpts.getInstance().getOptions().get(DataType.Mixed));
                default:
                    return ResponseEntity.ok(Collections.EMPTY_LIST);
            }
        } else {
            return ResponseEntity.ok(Collections.EMPTY_LIST);
        }
    }

}
