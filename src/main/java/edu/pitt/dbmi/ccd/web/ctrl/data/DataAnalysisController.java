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
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.service.VariableTypeService;
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
 * Jul 21, 2015 12:34:26 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/data/analysis")
public class DataAnalysisController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAnalysisController.class);

    private final VariableTypeService variableTypeService;

    @Autowired(required = true)
    public DataAnalysisController(VariableTypeService variableTypeService) {
        this.variableTypeService = variableTypeService;
    }

    @RequestMapping(value = "/analyze", method = RequestMethod.GET)
    public String getFileFInfo(
            @RequestParam(value = "file") String fileName,
            @ModelAttribute("appUser") AppUser appUser,
            Model model) throws IOException {
        model.addAttribute("fileName", fileName);

        List<AttributeValue> basicInfo = new LinkedList<>();
        Path file = Paths.get(appUser.getUploadDirectory(), fileName);
        try {
            BasicFileInfo info = FileInfos.basicPathInfo(file);
            basicInfo.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            basicInfo.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            basicInfo.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            basicInfo.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        model.addAttribute("basicInfo", basicInfo);

        model.addAttribute("variableTypes", variableTypeService.getAllVariableTypes());
//        model.addAttribute("fileDelimiters", fileDelimiterRepository.findAll());

        List<VariableType> variableTypes = variableTypeService.getAllVariableTypes();
        variableTypes.forEach(var -> {
            System.out.printf("%d) %s\n", var.getId(), var.getName());
        });

        return DATA_ANALYSIS;
    }

}
