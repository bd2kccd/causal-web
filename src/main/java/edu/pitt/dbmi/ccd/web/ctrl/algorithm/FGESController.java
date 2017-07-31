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

import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algorithm.FgescJobForm;
import edu.pitt.dbmi.ccd.web.model.algorithm.FgesdJobForm;
import edu.pitt.dbmi.ccd.web.model.algorithm.FgesmJobForm;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.algorithm.TetradJobService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jul 20, 2017 5:35:51 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/causal-discover/fges")
public class FGESController extends AbstractTetradController implements ViewPath {

    private final String fgesc;
    private final String fgesd;
    private final String fgesm;

    @Autowired
    public FGESController(
            @Value("${tetrad.algo.fges.fgesc}") String fgesc,
            @Value("${tetrad.algo.fges.fgesd}") String fgesd,
            @Value("${tetrad.algo.fges.fgesm}") String fgesm,
            TetradJobService tetradJobService,
            AppUserService appUserService) {
        super(tetradJobService, appUserService);
        this.fgesc = fgesc;
        this.fgesd = fgesd;
        this.fgesm = fgesm;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "${tetrad.algo.fges.fgesm}", method = RequestMethod.POST)
    public String submitFGESmJob(
            @Valid @ModelAttribute("tetradJobForm") final FgesmJobForm fgesmJobForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            @ModelAttribute("appUser") final AppUser appUser, final Model model) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tetradJobForm", bindingResult);
            redirAttrs.addFlashAttribute("tetradJobForm", fgesmJobForm);

            return REDIRECT_FGES_VIEW + fgesm;
        }

        return CAUSAL_DISCOVER_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.fges.fgesd}", method = RequestMethod.POST)
    public String submitFGESdJob(
            @Valid @ModelAttribute("tetradJobForm") final FgesdJobForm fgesdJobForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tetradJobForm", bindingResult);
            redirAttrs.addFlashAttribute("tetradJobForm", fgesdJobForm);

            return REDIRECT_FGES_VIEW + fgesd;
        }

        return CAUSAL_DISCOVER_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.fges.fgesc}", method = RequestMethod.POST)
    public String submitFGEScJob(
            @Valid @ModelAttribute("tetradJobForm") final FgescJobForm fgescJobForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tetradJobForm", bindingResult);
            redirAttrs.addFlashAttribute("tetradJobForm", fgescJobForm);

            return REDIRECT_FGES_VIEW + fgesc;
        }

        return CAUSAL_DISCOVER_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.fges.fgesm}", method = RequestMethod.GET)
    public String showFGESmView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        setupView(new FgesmJobForm(), FileVariableTypeService.MIXED_NAME, fgesm, appUser, model);

        return FGESM_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.fges.fgesd}", method = RequestMethod.GET)
    public String showFGESdView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        setupView(new FgesdJobForm(), FileVariableTypeService.DISCRETE_NAME, fgesd, appUser, model);

        return FGESD_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.fges.fgesc}", method = RequestMethod.GET)
    public String showFGEScView(@ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        setupView(new FgescJobForm(), FileVariableTypeService.CONTINUOUS_NAME, fgesc, appUser, model);

        return FGESC_VIEW;
    }

}
