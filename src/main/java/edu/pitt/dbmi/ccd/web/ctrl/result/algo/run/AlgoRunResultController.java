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
package edu.pitt.dbmi.ccd.web.ctrl.result.algo.run;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.INFO;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.result.algo.run.AlgoRunResultService;
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
@RequestMapping(value = "/secured/algorithm/result/run")
public class AlgoRunResultController implements ViewPath {

    private final AlgoRunResultService algoRunResultService;

    @Autowired
    public AlgoRunResultController(AlgoRunResultService algoRunResultService) {
        this.algoRunResultService = algoRunResultService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showResults(AppUser appUser, Model model) {
        algoRunResultService.listResults(appUser, model);

        return RESULT_LIST_VIEW;
    }

    @RequestMapping(value = INFO, method = RequestMethod.GET)
    public String showResultInfo(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        algoRunResultService.showResultInfo(id, appUser, model);

        return RESULT_INFO_VIEW;
    }

    @RequestMapping(value = "d3graph", method = RequestMethod.GET)
    public String showD3Graph(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        algoRunResultService.showD3Graph(id, appUser, model);

        return D3_GRAPH_VIEW;
    }

}
