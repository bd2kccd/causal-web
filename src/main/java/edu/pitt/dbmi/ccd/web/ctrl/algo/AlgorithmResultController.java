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
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.file.SelectedFiles;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmResultService;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private final List<String> categoryNames = Arrays.asList(
            "Runtime Parameters",
            "Dataset",
            "Filters",
            "FGS Parameters",
            "Run Options",
            "Algorithm Run",
            "Algorithm Parameters",
            "Data Validations"
    );

    private final AlgorithmResultService algorithmResultService;

    @Autowired
    public AlgorithmResultController(AlgorithmResultService algorithmResultService) {
        this.algorithmResultService = algorithmResultService;
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        algorithmResultService.downloadResultFile(fileName, appUser.getUsername(), request, response);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("itemList", algorithmResultService.listResultFileInfo(appUser.getUsername()));

        return ALGORITHM_RESULTS_VIEW;
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public String deleteResultFile(
            final SelectedFiles selectedFiles,
            @ModelAttribute("appUser") final AppUser appUser) {
        algorithmResultService.deleteResultFiles(selectedFiles.getFiles(), appUser.getUsername());

        return REDIRECT_ALGORITHM_RESULTS;
    }

    @RequestMapping(value = "error", method = RequestMethod.GET)
    public String showResultError(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("errors", algorithmResultService.getErrorMessages(fileName, appUser.getUsername()));

        return ALGORITHM_RESULT_ERROR_VIEW;
    }

    @RequestMapping(value = "plot", method = RequestMethod.GET)
    public String showPlot(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("plot", fileName);
        model.addAttribute("fileName", fileName);

        String username = appUser.getUsername();
        model.addAttribute("categories", algorithmResultService.extractDataCategories(fileName, username, categoryNames));
        model.addAttribute("isPag", algorithmResultService.isPagResult(fileName, username));

        return PLOT_VIEW;
    }

    @RequestMapping(value = "d3graph", method = RequestMethod.GET)
    public String showD3Graph(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("data", algorithmResultService.extractGraphNodes(fileName, appUser.getUsername()));

        return D3_GRAPH_VIEW;
    }

}
