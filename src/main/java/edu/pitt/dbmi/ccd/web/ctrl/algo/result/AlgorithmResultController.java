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
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CheckboxFiles;
import edu.pitt.dbmi.ccd.web.service.algo.result.AlgorithmResultService;
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
 * Jul 27, 2016 7:17:19 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/secured/algorithm/result")
public class AlgorithmResultController implements ViewPath {

    private final AlgorithmResultService algorithmResultService;

    @Autowired
    public AlgorithmResultController(AlgorithmResultService algorithmResultService) {
        this.algorithmResultService = algorithmResultService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showResults(AppUser appUser, Model model) {
        algorithmResultService.listResults(appUser, model);

        return RESULT_LIST_VIEW;
    }

    @RequestMapping(value = INFO, method = RequestMethod.GET)
    public String showResultInfo(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        algorithmResultService.showResultInfo(id, appUser, model);

        return RESULT_INFO_VIEW;
    }

    @RequestMapping(value = "d3graph", method = RequestMethod.GET)
    public String showD3Graph(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        algorithmResultService.showD3Graph(id, appUser, model);

        return D3_GRAPH_VIEW;
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public String deleteMultipleFiles(final CheckboxFiles checkboxFiles, @ModelAttribute("appUser") final AppUser appUser) {
        algorithmResultService.deleteMultipleFiles(checkboxFiles, appUser);

        return REDIRECT_ALGO_RESULT;
    }

}
