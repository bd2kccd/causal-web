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
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final ValidationException NO_SUCH_DATASET_EXCEPTION = new ValidationException("No such dataset.");

    private final TetradDataFileService tetradDataFileService;
    private final FileGroupService fileGroupService;

    @Autowired
    public TetradValidationService(TetradDataFileService tetradDataFileService, FileGroupService fileGroupService) {
        this.tetradDataFileService = tetradDataFileService;
        this.fileGroupService = fileGroupService;
    }

    public void validateDataset(Long datasetId, boolean isSingleFile) throws ValidationException {
        if (isSingleFile) {
            if (!tetradDataFileService.getRepository().existsById(datasetId)) {
                throw NO_SUCH_DATASET_EXCEPTION;
            }
        } else {
            if (!fileGroupService.getRepository().existsById(datasetId)) {
                throw NO_SUCH_DATASET_EXCEPTION;
            }
        }
    }

    public void validateExistence(String algorithmName, String testName, String scoreName) throws ValidationException {
        TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(algorithmName);
        if (algorithm == null) {
            throw NO_SUCH_ALGORITHM_EXCEPTION;
        }

        TetradScore score = TetradScores.getInstance().getTetradScore(scoreName);
        if (score == null && scoreName != null) {
            throw NO_SUCH_SCORE_EXCEPTION;
        }

        TetradTest test = TetradTests.getInstance().getTetradTest(testName);
        if (test == null && testName != null) {
            throw NO_SUCH_TEST_EXCEPTION;
        }
    }

    public void validateRequirement(TetradAlgorithm algorithm, TetradScore score, TetradTest test) throws ValidationException {
        if (algorithm == null) {
            throw ALGORITHM_REQUIRED_EXCEPTION;
        }

        boolean missingScore = algorithm.isRequiredScore() && (score == null);
        boolean missingTest = algorithm.isRequiredTest() && (test == null);
        if (missingTest || missingScore) {
            if (missingTest && missingScore) {
                throw TEST_SCORE_REQUIRED_EXCEPTION;
            } else if (missingTest) {
                throw TEST_REQUIRED_EXCEPTION;
            } else {
                throw SCORE_REQUIRED_EXCEPTION;
            }
        }
    }

}
