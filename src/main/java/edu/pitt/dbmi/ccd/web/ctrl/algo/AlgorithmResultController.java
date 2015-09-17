/*
 * Copyright (C) 2015 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.SelectedFiles;
import edu.pitt.dbmi.ccd.web.service.result.algorithm.AlgorithmResultService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Aug 7, 2015 12:45:54 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/results")
public class AlgorithmResultController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmResultController.class);

    private final AlgorithmResultService algorithmResultService;

    @Autowired(required = true)
    public AlgorithmResultController(AlgorithmResultService algorithmResultService) {
        this.algorithmResultService = algorithmResultService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        model.addAttribute("itemList", algorithmResultService.listResultFileInfo(appUser));

        return ALGORITHM_RESULTS_VIEW;
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public String deleteResultFile(
            final SelectedFiles selectedFiles,
            @ModelAttribute("appUser") final AppUser appUser) {
        algorithmResultService.deleteResultFile(selectedFiles.getFiles(), appUser);

        return REDIRECT_ALGORITHM_RESULTS;
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") final String fileName,
            @RequestParam(value = "remote") final boolean remote,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        algorithmResultService.downloadResultFile(fileName, remote, appUser, request, response);
    }

    @RequestMapping(value = PLOT, method = RequestMethod.GET)
    public String showPlot(
            @RequestParam(value = "file") final String fileName,
            @RequestParam(value = "remote") final boolean remote,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        String url = String.format("/algorithm/results/d3graph?file=%s&remote=%s", fileName, remote);
        model.addAttribute("plot", fileName);
        model.addAttribute("link", url);
        model.addAttribute("parameters", algorithmResultService.getPlotParameters(fileName, remote, appUser));

        return PLOT_VIEW;
    }

    @RequestMapping(value = D3_GRAPH, method = RequestMethod.GET)
    public String showD3Graph(
            @RequestParam(value = "file") final String fileName,
            @RequestParam(value = "remote") final boolean remote,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("data", algorithmResultService.getGraphNodes(fileName, remote, appUser));

        return D3_GRAPH_VIEW;
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String showResultError(
            @RequestParam(value = "file") final String fileName,
            @RequestParam(value = "remote") final boolean remote,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("errors", algorithmResultService.getErrorMessages(fileName, remote, appUser));

        return ALGORITHM_RESULT_ERROR_VIEW;
    }

}
