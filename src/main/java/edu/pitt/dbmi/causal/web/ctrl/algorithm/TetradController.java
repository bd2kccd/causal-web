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

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.service.algorithm.TetradService;
import edu.pitt.dbmi.causal.web.tetrad.AlgorithmOpts;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Mar 5, 2018 1:28:39 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/tetrad")
public class TetradController {

    private final TetradService tetradService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public TetradController(TetradService tetradService, VariableTypeService variableTypeService) {
        this.tetradService = tetradService;
        this.variableTypeService = variableTypeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runTetrad(
            @Valid @ModelAttribute("tetradForm") final TetradForm tetradForm,
            final BindingResult bindingResult,
            final Model model,
            final AppUser appUser,
            final RedirectAttributes redirAttrs) {
        if (!model.containsAttribute("tetradForm")) {
            model.addAttribute("tetradForm", tetradForm);
        }
        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("algoOpts", AlgorithmOpts.getInstance().getOptions());

        return ViewPath.TETRAD_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showTetradView(
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        if (!model.containsAttribute("tetradForm")) {
            model.addAttribute("tetradForm", tetradService.createTetradForm());
        }

        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("algoOpts", AlgorithmOpts.getInstance().getOptions());

        return ViewPath.TETRAD_VIEW;
    }

}
