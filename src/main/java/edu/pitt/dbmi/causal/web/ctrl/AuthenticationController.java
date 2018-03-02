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

import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.LoginForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.AuthService;
import edu.pitt.dbmi.causal.web.service.filesys.FileManagementService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
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
 * Aug 3, 2017 11:47:41 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class AuthenticationController {

    private static final String[] INVALID_CREDENTIALS = {"Login Failed!", "Invalid username and/or password."};
    private static final String[] UNACTIVATED_ACCOUNT = {"Login Failed!", "Your account has not been activated."};

    private final AuthService authService;
    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;

    @Autowired
    public AuthenticationController(
            AuthService authService,
            AppUserService appUserService,
            FileManagementService fileManagementService) {
        this.authService = authService;
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
    }

    @RequestMapping(value = ViewPath.LOGIN, method = RequestMethod.POST)
    public String logIn(
            @Valid @ModelAttribute("loginForm") final LoginForm loginForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            final Model model,
            final HttpServletRequest req) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.loginForm", bindingResult);
            redirAttrs.addFlashAttribute("loginForm", loginForm);

            return ViewPath.REDIRECT_LOGIN;
        }

        Subject currentUser = authService.login(loginForm);
        if (currentUser.isAuthenticated()) {
            UserAccount userAccount = authService.retrieveAccount(currentUser);
            if (userAccount != null && userAccount.isActivated()) {
                fileManagementService.setupUserHomeDirectory(userAccount);

                AppUser appUser = appUserService
                        .create(userAccount, false, req.getRemoteAddr());
                redirAttrs.addFlashAttribute("appUser", appUser);

                return ViewPath.REDIRECT_HOME;
            } else {
                currentUser.logout();
                redirAttrs.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);

                return ViewPath.REDIRECT_LOGIN;
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", INVALID_CREDENTIALS);
            redirAttrs.addFlashAttribute("loginForm", loginForm);

            return ViewPath.REDIRECT_LOGIN;
        }
    }

}
