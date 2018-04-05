/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.ctrl;

import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Feb 18, 2016 1:29:10 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class ApplicationController {

    private final AppUserService appUserService;
    private final FileService fileService;
    private final FileGroupService fileGroupService;

    @Autowired
    public ApplicationController(AppUserService appUserService, FileService fileService, FileGroupService fileGroupService) {
        this.appUserService = appUserService;
        this.fileService = fileService;
        this.fileGroupService = fileGroupService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage() {
        return ViewPath.REDIRECT_LOGIN;
    }

    @RequestMapping(value = ViewPath.HOME, method = RequestMethod.GET)
    public String showHomePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        model.addAttribute("numOfFiles", fileService.getRepository().countByUserAccount(userAccount));
        model.addAttribute("numOfFileGroups", fileGroupService.getRepository().countByUserAccount(userAccount));

        return ViewPath.HOME_VIEW;
    }

    @RequestMapping(value = ViewPath.MESSAGE, method = RequestMethod.GET)
    public String showMessage(final Model model) {
        if (!model.containsAttribute("message")) {
            throw new ResourceNotFoundException();
        }

        return ViewPath.MESSAGE_VIEW;
    }

}
