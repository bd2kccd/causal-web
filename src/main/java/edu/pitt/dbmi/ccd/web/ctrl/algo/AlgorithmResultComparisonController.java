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

import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphComparison;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.SelectedFiles;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparisonData;
import edu.pitt.dbmi.ccd.web.service.result.compare.ResultComparisonService;
import java.util.List;
import java.util.Set;
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
 * Sep 8, 2015 10:59:43 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/results/comparison")
public class AlgorithmResultComparisonController implements ViewPath {

    private final ResultComparisonService resultComparisonService;

    @Autowired(required = true)
    public AlgorithmResultComparisonController(ResultComparisonService resultComparisonService) {
        this.resultComparisonService = resultComparisonService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        model.addAttribute("itemList", resultComparisonService.list(appUser));

        return ALGO_RESULT_COMPARE_VIEW;
    }

    @RequestMapping(value = "table", method = RequestMethod.GET)
    public String showResultComparisonTableView(
            @RequestParam(value = "file") final String fileName,
            @RequestParam(value = "remote") final boolean remote,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("item", resultComparisonService.readInResultComparisonFile(fileName, remote, appUser));

        return ALGO_RESULT_COMPARISON_TABLE_VIEW;
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public String deleteResultFile(
            final SelectedFiles selectedFiles,
            @ModelAttribute("appUser") final AppUser appUser) {
        resultComparisonService.delete(selectedFiles.getFiles(), appUser);

        return REDIRECT_ALGO_RESULT_COMPARE_VIEW;
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") final String fileName,
            @RequestParam(value = "remote") final boolean remote,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        resultComparisonService.download(fileName, remote, appUser, request, response);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String compareResultFile(
            final SelectedFiles selectedFiles,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        List<String> fileNames = selectedFiles.getFiles();
        if (!fileNames.isEmpty()) {
            SimpleGraphComparison simpleGraphComparison = new SimpleGraphComparison();
            simpleGraphComparison.compare(resultComparisonService.compareResultFile(fileNames, appUser));

            Set<String> distinctEdges = simpleGraphComparison.getDistinctEdges();
            Set<String> edgesInAll = simpleGraphComparison.getEdgesInAll();
            Set<String> sameEndPoints = simpleGraphComparison.getSameEndPoints();

            String fileName = "result_comparison_" + System.currentTimeMillis() + ".txt";

            ResultComparison resultComparison = new ResultComparison(fileName);
            resultComparison.getFileNames().addAll(fileNames);

            List<ResultComparisonData> comparisonResults = resultComparison.getComparisonData();
            int countIndex = 0;
            for (String edge : distinctEdges) {
                ResultComparisonData rc = new ResultComparisonData(edge);
                rc.setInAll(edgesInAll.contains(edge));
                rc.setSimilarEndPoint(sameEndPoints.contains(edge));
                rc.setCountIndex(++countIndex);

                comparisonResults.add(rc);
            }

            resultComparisonService.writeResultComparison(resultComparison, fileName, appUser);
        }

        return REDIRECT_ALGO_RESULT_COMPARE_VIEW;
    }

}
