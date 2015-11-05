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
package edu.pitt.dbmi.ccd.web.ctrl.anno;

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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.service.DataService;

/**
 * 
 * Nov 5, 2015 11:15:12 AM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/data/annotation")
public class DataAnnotationController implements ViewPath {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataAnnotationController.class);
	
    private final DataService dataService;

    @Autowired(required = true)
    public DataAnnotationController(DataService dataService) {
        this.dataService = dataService;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String showDataAnnotationView(
    		@RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model){

        String baseDir = appUser.getDataDirectory();
        Path file = Paths.get(baseDir, fileName);

        model.addAttribute("fileName", fileName);
        model.addAttribute("basicInfo", getFileBasicInfo(file));
        model.addAttribute("additionalInfo", getFileAdditionalInfo(baseDir, fileName));

    	return DATA_ANNOTATION_VIEW;
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
