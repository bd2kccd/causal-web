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

import edu.pitt.dbmi.causal.web.ctrl.SitePaths;
import edu.pitt.dbmi.causal.web.ctrl.SiteViews;
import edu.pitt.dbmi.causal.web.exception.ValidationException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.algorithm.TetradJobSubmissionService;
import edu.pitt.dbmi.causal.web.service.algorithm.TetradValidationService;
import edu.pitt.dbmi.causal.web.tetrad.AlgoTypes;
import edu.pitt.dbmi.ccd.db.code.VariableTypeCodes;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Mar 5, 2018 1:28:39 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/tetrad")
public class TetradController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TetradController.class);

    private final TetradJobSubmissionService tetradJobSubmissionService;
    private final TetradValidationService tetradValidationService;
    private final VariableTypeService variableTypeService;
    private final AppUserService appUserService;

    @Autowired
    public TetradController(TetradJobSubmissionService tetradJobSubmissionService, TetradValidationService tetradValidationService, VariableTypeService variableTypeService, AppUserService appUserService) {
        this.tetradJobSubmissionService = tetradJobSubmissionService;
        this.tetradValidationService = tetradValidationService;
        this.variableTypeService = variableTypeService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping
    public String addToJobQueue(
            @Valid @ModelAttribute("tetradForm") final TetradForm tetradForm,
            final BindingResult bindingResult,
            @RequestBody MultiValueMap<String, String> formData,
            final Model model,
            final AppUser appUser,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tetradForm", bindingResult);
            redirAttrs.addFlashAttribute("tetradForm", tetradForm);

            return SitePaths.REDIRECT_TETRAD;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        try {
            tetradValidationService.validate(tetradForm, formData, userAccount);
        } catch (ValidationException exception) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tetradForm", bindingResult);
            redirAttrs.addFlashAttribute("tetradForm", tetradForm);
            redirAttrs.addFlashAttribute("errorMsg", exception.getErrorMessage());

            return SitePaths.REDIRECT_TETRAD;
        }

        try {
            tetradJobSubmissionService.submitJob(tetradForm, formData, userAccount);
        } catch (Exception exception) {
            String errMsg = "Unable to submit job.";
            LOGGER.error(errMsg, exception);

            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tetradForm", bindingResult);
            redirAttrs.addFlashAttribute("tetradForm", tetradForm);
            redirAttrs.addFlashAttribute("errorMsg", errMsg);

            return SitePaths.REDIRECT_TETRAD;
        }

        return SitePaths.REDIRECT_JOB_QUEUE;
    }

    @GetMapping
    public String show(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        if (!model.containsAttribute("tetradForm")) {
            TetradForm tetradForm = new TetradForm();
            tetradForm.setVarTypeId(variableTypeService.findByCode(VariableTypeCodes.CONTINUOUS).getId());
            tetradForm.setAlgoType(AlgoTypes.DEFAULT_VALUE);

            model.addAttribute("tetradForm", tetradForm);
        }

        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("algoTypes", AlgoTypes.getInstance().getOptions());

        return SiteViews.TETRAD;
    }

}
