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
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Autowired(required = true)
    public DataManagementController(DataService dataService) {
        this.dataService = dataService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDatasetView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        model.addAttribute("itemList", dataService.createListItem(appUser.getUsername(), appUser.getDataDirectory()));

        return DATASET_VIEW;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteResultFile(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        String dataDir = appUser.getDataDirectory();
        Path file = Paths.get(dataDir, filename);
        if (dataService.getDataFileService().deleteDataFileByNameAndAbsolutePath(dataDir, filename)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {
                LOGGER.error(
                        String.format("Unable to delete file %s.", file.toAbsolutePath().toString()),
                        exception);
            }
        }

        return REDIRECT_DATA;
    }

    @RequestMapping(value = "/example", method = RequestMethod.GET)
    public String getExampleFileFInfo(@RequestParam(value = "type") String type) {
        return "data/example/" + type;
    }

    @RequestMapping(value = FILE_INFO, method = RequestMethod.GET)
    public String getFileFInfo(
            @RequestParam(value = "file") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) throws IOException {
        model.addAttribute("basicInfo", dataService.getFileInfo(appUser.getDataDirectory(), fileName));

        return FILE_INFO_VIEW;
    }

}
