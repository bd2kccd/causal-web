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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.model.data.DataSummary;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
 * Aug 6, 2015 8:10:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/data/summary")
public class DataSummaryController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSummaryController.class);

    private final VariableTypeService variableTypeService;

    private final FileDelimiterService fileDelimiterService;

    private final DataService dataService;

    @Autowired(required = true)
    public DataSummaryController(
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            DataService dataService) {
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.dataService = dataService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runDataSummary(
            @ModelAttribute("dataSummary") final DataSummary dataSummary,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        String baseDir = appUser.getDataDirectory();
        String fileName = dataSummary.getFileName();
        Path file = Paths.get(baseDir, fileName);

        model.addAttribute("fileName", fileName);
        model.addAttribute("basicInfo", getFileBasicInfo(file));
        model.addAttribute("additionalInfo", getFileAdditionalInfo(baseDir, fileName, dataSummary));
        model.addAttribute("dataSummary", dataService.getDataSummary(baseDir, fileName));
        model.addAttribute("variableTypes", variableTypeService.findAll());
        model.addAttribute("fileDelimiters", fileDelimiterService.findAll());

        return REDIRECT_DATA;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDataSummary(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        String baseDir = appUser.getDataDirectory();
        Path file = Paths.get(baseDir, fileName);

        model.addAttribute("fileName", fileName);
        model.addAttribute("basicInfo", getFileBasicInfo(file));
        model.addAttribute("additionalInfo", getFileAdditionalInfo(baseDir, fileName));
        model.addAttribute("dataSummary", dataService.getDataSummary(baseDir, fileName));
        model.addAttribute("variableTypes", variableTypeService.findAll());
        model.addAttribute("fileDelimiters", fileDelimiterService.findAll());

        return DATA_SUMMARY_VIEW;
    }

    private List<AttributeValue> getFileAdditionalInfo(String absolutePath, String name, DataSummary dataSummary) {
        return dataService.getDataFileAdditionalInfo(absolutePath, name, dataSummary);
    }

    private List<AttributeValue> getFileAdditionalInfo(String absolutePath, String name) {
        return dataService.getDataFileAdditionalInfo(absolutePath, name);
    }

    private List<AttributeValue> getFileBasicInfo(Path file) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        try {
            BasicFileInfo info = FileInfos.basicPathInfo(file);
            fileInfo.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            fileInfo.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            fileInfo.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            fileInfo.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return fileInfo;
    }

}
