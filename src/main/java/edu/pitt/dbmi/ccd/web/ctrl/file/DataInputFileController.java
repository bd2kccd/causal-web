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
package edu.pitt.dbmi.ccd.web.ctrl.file;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.file.DataInputFileService;
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
 * Jul 25, 2016 4:53:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/input/data")
public class DataInputFileController implements ViewPath {

    private final DataInputFileService dataInputFileService;

    @Autowired
    public DataInputFileController(DataInputFileService dataInputFileService) {
        this.dataInputFileService = dataInputFileService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFiles(AppUser appUser, Model model) {
        dataInputFileService.listFiles(appUser, model);

        return FILE_LIST_VIEW;
    }

    @RequestMapping(value = INFO, method = RequestMethod.GET)
    public String showFileInfo(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        dataInputFileService.showFileInfo(id, appUser, model);

        return FILE_INFO_VIEW;
    }

}
