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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.LoginCredentials;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.ShiroAuthService;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Feb 18, 2016 1:29:10 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class ApplicationController implements ViewPath {

    private final ShiroAuthService shiroAuthService;

    private final FileManagementService fileManagementService;

    @Autowired
    public ApplicationController(ShiroAuthService shiroAuthService, FileManagementService fileManagementService) {
        this.shiroAuthService = shiroAuthService;
        this.fileManagementService = fileManagementService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage() {
        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(
            @Valid @ModelAttribute("loginCredentials") final LoginCredentials loginCredentials,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final Model model,
            final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loginCredentials", bindingResult);
            redirectAttributes.addFlashAttribute("loginCredentials", loginCredentials);
            return REDIRECT_LOGIN;
        }

        boolean loginSuccess = shiroAuthService.logInUser(loginCredentials, redirectAttributes, model, request);

        return loginSuccess ? REDIRECT_HOME : REDIRECT_LOGIN;
    }

    @RequestMapping(value = LOGOUT, method = RequestMethod.GET)
    public String logOut(
            @ModelAttribute("appUser") final AppUser appUser,
            final SessionStatus sessionStatus,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        shiroAuthService.logOutUser(appUser, sessionStatus, redirectAttributes, request);

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String showHomePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        fileManagementService.showSummaryCounts(appUser, model);

        return HOME_VIEW;
    }

    @RequestMapping(value = MESSAGE, method = RequestMethod.GET)
    public String showMessage(final Model model) {
        if (!model.containsAttribute("header")) {
            throw new ResourceNotFoundException();
        }

        return MESSAGE_VIEW;
    }

}
