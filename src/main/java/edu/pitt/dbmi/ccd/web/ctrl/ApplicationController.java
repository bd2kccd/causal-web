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
package edu.pitt.dbmi.ccd.web.ctrl;

import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.HOME;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.HOME_VIEW;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.LOGIN;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.LOGOUT;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_HOME;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_LOGIN;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.LoginCredentials;
import edu.pitt.dbmi.ccd.web.service.ApplicationService;
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
 * May 14, 2015 12:39:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class ApplicationController implements ViewPath {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String showHomePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        applicationService.retrieveFileCounts(appUser, model);

        return HOME_VIEW;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage() {
        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String logIn(
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

        boolean isAuthenticated = applicationService.logInUser(loginCredentials, redirectAttributes, model, request);

        return isAuthenticated ? REDIRECT_HOME : REDIRECT_LOGIN;
    }

    @RequestMapping(value = LOGOUT, method = RequestMethod.GET)
    public String logOut(
            @ModelAttribute("appUser") final AppUser appUser,
            final SessionStatus sessionStatus,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        applicationService.logOutUser(appUser, sessionStatus, redirectAttributes, request);

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = MESSAGE, method = RequestMethod.GET)
    public String showMessage(final Model model) {
        if (!model.containsAttribute("header")) {
            throw new ResourceNotFoundException();
        }

        return MESSAGE_VIEW;
    }

}
