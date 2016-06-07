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
package edu.pitt.dbmi.ccd.web.ctrl.data;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.data.DataSummary;
import edu.pitt.dbmi.ccd.web.service.DataService;
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
 * Aug 6, 2015 2:56:26 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "data")
public class DataManagementController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagementController.class);

    private final DataService dataService;

    @Autowired
    public DataManagementController(DataService dataService) {
        this.dataService = dataService;
    }

    @RequestMapping(value = "summary", method = RequestMethod.POST)
    public String summarizeDataFile(
            @ModelAttribute("dataSummary") final DataSummary dataSummary,
            @ModelAttribute("appUser") final AppUser appUser) {
        dataService.saveDataSummary(dataSummary, appUser.getUsername());

        return REDIRECT_DATA;
    }

    @RequestMapping(value = "summary", method = RequestMethod.GET)
    public String showDataFileSummary(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        String username = appUser.getUsername();
        model.addAttribute("fileName", fileName);
        model.addAttribute("basicInfo", dataService.getFileInfo(fileName, username));
        model.addAttribute("summaryInfo", dataService.getDataFileAdditionalInfo(fileName, username));
        model.addAttribute("dataSummary", dataService.getDataSummary(fileName, username));
        model.addAttribute("variableTypes", dataService.getVariableTypes());
        model.addAttribute("fileDelimiters", dataService.getFileDelimiters());

        return DATA_SUMMARY_VIEW;
    }

    @RequestMapping(value = "annotations", method = RequestMethod.GET)
    public String showDataFileAnnotations(
            @RequestParam(value = "file") final String fileName,
            final Model model) {
        model.addAttribute("fileName", fileName);

        return DATA_ANNOTATIONS_VIEW;
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String deleteDataFile(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        dataService.deleteDataFile(fileName, appUser.getUsername());

        return REDIRECT_DATA;
    }

    @RequestMapping(value = "fileInfo", method = RequestMethod.GET)
    public String getDataFileFInfo(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("basicInfo", dataService.getFileInfo(fileName, appUser.getUsername()));
        model.addAttribute("summaryInfo", dataService.getDataFileAdditionalInfo(fileName, appUser.getUsername()));

        return FILE_INFO_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDataFilePage(
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        model.addAttribute("itemList", dataService.listDataFiles(appUser.getUsername()));

        return DATASET_VIEW;
    }

}
