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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserEventLogService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.LoginForm;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.AuthenticationService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
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

    private static final String[] INVALID_CREDENTIALS = {"Login Failed!", "Invalid username and/or password."};
    private static final String[] UNACTIVATED_ACCOUNT = {"Login Failed!", "Your account has not been activated."};

    private final AuthenticationService authenticationService;
    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final UserEventLogService userEventLogService;

    @Autowired
    public ApplicationController(
            AuthenticationService authenticationService,
            AppUserService appUserService,
            FileManagementService fileManagementService,
            UserEventLogService userEventLogService) {
        this.authenticationService = authenticationService;
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.userEventLogService = userEventLogService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage() {
        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String logIn(
            @Valid @ModelAttribute("loginForm") final LoginForm loginForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            final Model model,
            final HttpServletRequest req) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.loginForm", bindingResult);
            redirAttrs.addFlashAttribute("loginForm", loginForm);

            return REDIRECT_LOGIN;
        }

        Subject currentUser = authenticationService.loginWithUsernamePassword(loginForm);
        if (currentUser.isAuthenticated()) {
            UserAccount userAccount = authenticationService.retrieveUserAccount(currentUser);
            if (userAccount != null && userAccount.isActivated()) {
                userEventLogService.logUserLogin(userAccount);
                fileManagementService.setupUserHomeDirectory(userAccount);
                redirAttrs.addFlashAttribute("appUser", appUserService.create(userAccount, false));
                authenticationService.setLoginInfo(userAccount, req.getRemoteAddr());

                return REDIRECT_HOME;
            } else {
                currentUser.logout();
                redirAttrs.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);

                return REDIRECT_LOGIN;
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", INVALID_CREDENTIALS);
            redirAttrs.addFlashAttribute("loginForm", loginForm);

            return REDIRECT_LOGIN;
        }
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String showHomePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {

        return HOME_VIEW;
    }

    @RequestMapping(value = MESSAGE, method = RequestMethod.GET)
    public String showMessage(final Model model) {
        if (!model.containsAttribute("message")) {
            throw new ResourceNotFoundException();
        }

        return MESSAGE_VIEW;
    }

}
