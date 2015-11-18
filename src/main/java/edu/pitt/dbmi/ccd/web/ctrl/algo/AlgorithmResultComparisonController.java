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
import edu.pitt.dbmi.ccd.web.service.algo.ResultComparisonService;
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
 * Nov 16, 2015 10:58:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/results/comparison")
public class AlgorithmResultComparisonController implements ViewPath {

    private final ResultComparisonService resultComparisonService;

    @Autowired
    public AlgorithmResultComparisonController(ResultComparisonService resultComparisonService) {
        this.resultComparisonService = resultComparisonService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        model.addAttribute("itemList", resultComparisonService.list(appUser.getUsername()));

        return ALGO_RESULT_COMPARE_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String compareResultFile(
            final SelectedFiles selectedFiles,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        resultComparisonService.compareResultFile(selectedFiles.getFiles(), appUser.getUsername());

        return REDIRECT_ALGO_RESULT_COMPARE_VIEW;
    }

    @RequestMapping(value = "table", method = RequestMethod.GET)
    public String showResultComparisonTableView(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("item", resultComparisonService.readInResultComparisonFile(fileName, appUser.getUsername()));

        return ALGO_RESULT_COMPARISON_TABLE_VIEW;
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        resultComparisonService.download(fileName, appUser.getUsername(), request, response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public String deleteResultFile(
            final SelectedFiles selectedFiles,
            @ModelAttribute("appUser") final AppUser appUser) {
        resultComparisonService.delete(selectedFiles.getFiles(), appUser.getUsername());

        return REDIRECT_ALGO_RESULT_COMPARE_VIEW;
    }

}
