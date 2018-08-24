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

import edu.pitt.dbmi.causal.web.exception.ValidationException;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.ccd.db.code.FileFormatCodes;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 *
 * Apr 10, 2018 2:15:01 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class TetradValidationService {

    private final ValidationException NO_SUCH_ALGORITHM_EXCEPTION = new ValidationException("No such algorithm found.");
    private final ValidationException NO_SUCH_TEST_EXCEPTION = new ValidationException("No such test found.");
    private final ValidationException NO_SUCH_SCORE_EXCEPTION = new ValidationException("No such score found.");

    private final ValidationException ALGORITHM_REQUIRED_EXCEPTION = new ValidationException("Algorithm is required.");
    private final ValidationException SCORE_REQUIRED_EXCEPTION = new ValidationException("Score is required.");
    private final ValidationException TEST_REQUIRED_EXCEPTION = new ValidationException("Test is required.");
    private final ValidationException TEST_SCORE_REQUIRED_EXCEPTION = new ValidationException("Both test and score is required.");

    private final ValidationException DATASET_REQUIRED_EXCEPTION = new ValidationException("Dataset file is required.");
    private final ValidationException NO_SUCH_DATASET_EXCEPTION = new ValidationException("No such dataset file.");
    private final ValidationException NO_SUCH_KNOWLEDGE_EXCEPTION = new ValidationException("No such knowledge file.");
    private final ValidationException NO_SUCH_VARIABLE_EXCEPTION = new ValidationException("No such variable file.");

    private final FileGroupService fileGroupService;
    private final FileFormatService fileFormatService;
    private final FileService fileService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;

    @Autowired
    public TetradValidationService(FileGroupService fileGroupService, FileFormatService fileFormatService, FileService fileService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService) {
        this.fileGroupService = fileGroupService;
        this.fileFormatService = fileFormatService;
        this.fileService = fileService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
    }

    public void validate(TetradForm tetradForm, MultiValueMap<String, String> formData, UserAccount userAccount) throws ValidationException {
        validateFile(tetradForm, userAccount);
        validateAlgorithm(tetradForm);
    }

    private void validateAlgorithm(TetradForm tetradForm) {
        String algoCmd = tetradForm.getAlgorithm();
        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algoCmd);
        if (algorithm == null) {
            throw NO_SUCH_ALGORITHM_EXCEPTION;
        }

        String scoreCmd = tetradForm.getScore();
        TetradScore score = TetradScores.getInstance().getTetradScore(scoreCmd);
        if (scoreCmd != null && score == null) {
            throw NO_SUCH_SCORE_EXCEPTION;
        }

        String testCmd = tetradForm.getTest();
        TetradTest test = TetradTests.getInstance().getTetradTest(testCmd);
        if (testCmd != null && test == null) {
            throw NO_SUCH_TEST_EXCEPTION;
        }

        boolean missingScore = algorithm.isRequiredScore() && (score == null);
        boolean missingTest = algorithm.isRequiredTest() && (test == null);
        if (missingTest && missingScore) {
            throw TEST_SCORE_REQUIRED_EXCEPTION;
        } else if (missingTest) {
            throw TEST_REQUIRED_EXCEPTION;
        } else if (missingScore) {
            throw SCORE_REQUIRED_EXCEPTION;
        }
    }

    private void validateFile(TetradForm tetradForm, UserAccount userAccount) throws ValidationException {
        // validate dataset
        boolean singleDataFile = tetradForm.isSingleDataFile();
        Long id = tetradForm.getDataFileId();
        if (id == null) {
            throw DATASET_REQUIRED_EXCEPTION;
        } else {
            if (singleDataFile) {
                if (!tetradDataFileService.getRepository().existsByIdAndUserAccount(id, userAccount)) {
                    throw NO_SUCH_DATASET_EXCEPTION;
                }
            } else {
                if (!fileGroupService.getRepository().existsByIdAndUserAccount(id, userAccount)) {
                    throw NO_SUCH_DATASET_EXCEPTION;
                }
            }
        }

        // validate knowledge file
        id = tetradForm.getKnwlFileId();
        if (id != null) {
            FileFormat fileFormat = fileFormatService.findByCode(FileFormatCodes.TETRAD_KNWL);
            if (!fileService.getRepository().existsByIdAndFileFormatAndUserAccount(id, fileFormat, userAccount)) {
                throw NO_SUCH_KNOWLEDGE_EXCEPTION;
            }
        }

        // validate variable file
        id = tetradForm.getVarFileId();
        if (id != null) {
            if (!tetradVariableFileService.getRepository().existsByIdAndUserAccount(id, userAccount)) {
                throw NO_SUCH_VARIABLE_EXCEPTION;
            }
        }
    }

}
