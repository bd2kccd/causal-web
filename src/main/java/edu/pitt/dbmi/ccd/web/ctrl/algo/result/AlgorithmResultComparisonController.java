/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.ctrl.algo.result;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.INFO;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CheckboxFiles;
import edu.pitt.dbmi.ccd.web.service.algo.result.AlgorithmResultComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Aug 19, 2016 4:21:08 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/secured/algorithm/result/comparison")
public class AlgorithmResultComparisonController implements ViewPath {

    private final AlgorithmResultComparisonService algorithmResultComparisonService;

    @Autowired
    public AlgorithmResultComparisonController(AlgorithmResultComparisonService algorithmResultComparisonService) {
        this.algorithmResultComparisonService = algorithmResultComparisonService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showResultComparison(AppUser appUser, Model model) {
        algorithmResultComparisonService.listResults(appUser, model);

        return RESULT_COMPARISON_LIST_VIEW;
    }

    @RequestMapping(value = "compare", method = RequestMethod.POST)
    public String compare(final CheckboxFiles checkboxFiles, @ModelAttribute("appUser") final AppUser appUser) {
        algorithmResultComparisonService.compare(checkboxFiles, appUser);

        return REDIRECT_RESULT_COMPARISON;
    }

    @RequestMapping(value = INFO, method = RequestMethod.GET)
    public String showFileInfo(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        algorithmResultComparisonService.showFileInfo(id, appUser, model);

        return RESULT_COMPARISON_VIEW;
    }

}
