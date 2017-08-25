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
package edu.pitt.dbmi.causal.web.ctrl.algorithm;

import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradJobForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.algorithm.TetradJobService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import java.util.List;
import org.springframework.ui.Model;

/**
 *
 * Jul 27, 2017 2:53:08 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractTetradController {

    private final TetradJobService tetradJobService;
    private final AppUserService appUserService;

    public AbstractTetradController(TetradJobService tetradJobService, AppUserService appUserService) {
        this.tetradJobService = tetradJobService;
        this.appUserService = appUserService;
    }

    protected void setupView(TetradJobForm tetradJobForm, String fileVariableTypeName, String algorithm, AppUser appUser, Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        List<File> datasetOpts = tetradJobService.getDatasetOpts(fileVariableTypeName, userAccount);
        List<File> knowledgeOpts = tetradJobService.getKnowledgeOpts(userAccount);
        List<File> excludeVariableOpts = tetradJobService.getVariableOpts(userAccount);

        if (!model.containsAttribute("tetradJobForm")) {
            if (!datasetOpts.isEmpty()) {
                tetradJobForm.setDataset(datasetOpts.get(0).getTitle());
            }
            if (!knowledgeOpts.isEmpty()) {
                tetradJobForm.setKnowledge(knowledgeOpts.get(0).getTitle());
            }
            if (!excludeVariableOpts.isEmpty()) {
                tetradJobForm.setExcludeVariable(excludeVariableOpts.get(0).getTitle());
            }

            model.addAttribute("tetradJobForm", tetradJobForm);
        }

        model.addAttribute("title", tetradJobService.getAlgorithmTitle(algorithm));
        model.addAttribute("datasetOpts", datasetOpts);
        model.addAttribute("knowledgeOpts", knowledgeOpts);
        model.addAttribute("excludeVariableOpts", excludeVariableOpts);
    }

}
