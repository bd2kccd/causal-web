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
package edu.pitt.dbmi.ccd.web.ctrl.algorithm;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.algo.FgescForm;
import edu.pitt.dbmi.ccd.web.prop.TetradProperties;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 20, 2017 5:35:51 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/causal-discover/fges")
public class FGESController implements ViewPath {

    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final AppUserService appUserService;
    private final TetradProperties tetradProperties;

    @Autowired
    public FGESController(FileService fileService, FileFormatService fileFormatService, AppUserService appUserService, TetradProperties tetradProperties) {
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.appUserService = appUserService;
        this.tetradProperties = tetradProperties;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @RequestMapping(value = "${algo.fges.fgesc}", method = RequestMethod.GET)
    public String showFGEScView(@Value("${algo.fges.fgesc}") final String algoName, @ModelAttribute("appUser") final AppUser appUser, final Model model) {
        String title = tetradProperties.getAlgoTypeTitles().get(algoName);
        String description = tetradProperties.getAlgoTypeDescription().get(algoName);

        FgescForm fgescForm = new FgescForm();

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        FileFormat fileFormat = fileFormatService.findByName(FileFormatService.TETRAD_TABULAR_NAME);
        List<File> datasetList = fileService.getRepository().findByUserAccountAndFileFormat(userAccount, fileFormat);
        if (!datasetList.isEmpty()) {
            fgescForm.setDataset(datasetList.get(0).getTitle());
        }

        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("fgescForm", fgescForm);
        model.addAttribute("datasetList", datasetList);

        return FGES_VIEW;
    }

}
