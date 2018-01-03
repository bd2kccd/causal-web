/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.TetradAlgoOpt;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.util.TetradCmdOptions;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * May 28, 2017 11:28:37 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractTetradAlgoController {

    private final DataFileService dataFileService;
    private final AppUserService appUserService;

    public AbstractTetradAlgoController(DataFileService dataFileService, AppUserService appUserService) {
        this.dataFileService = dataFileService;
        this.appUserService = appUserService;
    }

    protected Map<String, String> getFileSummary(TetradAlgoOpt algoOpt, AppUser appUser) {
        Map<String, String> params = new HashMap<>();

        UserAccount userAccount = appUserService.getUserAccount(appUser);
        DataFile dataFile = dataFileService.findByNameAndUserAccounts(algoOpt.getDataset(), Collections.singleton(userAccount));
        if (dataFile != null) {
            params.put("fileSize", Long.toString(dataFile.getFileSize()));

            DataFileInfo fileInfo = dataFile.getDataFileInfo();
            if (fileInfo != null) {
                params.put("numOfColumns", Integer.toString(fileInfo.getNumOfColumns()));
                params.put("numOfRows", Integer.toString(fileInfo.getNumOfRows()));
            }
        }

        return params;
    }

    protected List<String> getDataset(TetradAlgoOpt tetradAlgoOpt) {
        return Collections.singletonList(tetradAlgoOpt.getDataset());
    }

    protected List<String> getPriorKnowledge(TetradAlgoOpt tetradAlgoOpt) {
        String priorKnowledge = tetradAlgoOpt.getPriorKnowledge();
        if (priorKnowledge.trim().length() == 0) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.singletonList(tetradAlgoOpt.getPriorKnowledge());
        }
    }

    protected List<String> getJvmOptions(TetradAlgoOpt tetradAlgoOpt) {
        List<String> jvmOptions = new LinkedList<>();

        int jvmMaxMem = tetradAlgoOpt.getJvmMaxMem();
        if (jvmMaxMem > 0) {
            jvmOptions.add(String.format("-Xmx%dG", jvmMaxMem));
        }

        return jvmOptions;
    }

    protected void getBootstrapParameters(TetradAlgoOpt tetradAlgoOpt, List<String> parameters) {
        parameters.add(TetradCmdOptions.BOOTSTRAP_ENSEMBLE);
        parameters.add(Integer.toString(tetradAlgoOpt.getBootstrapEnsemble()));
        parameters.add(TetradCmdOptions.BOOTSTRAP_SAMPLE_SIZE);
        parameters.add(Integer.toString(tetradAlgoOpt.getBootstrapSampleSize()));
    }

    protected void setCommonParameters(List<String> parameters) {
        parameters.add(TetradCmdOptions.JSON_GRAPH);
    }

}
