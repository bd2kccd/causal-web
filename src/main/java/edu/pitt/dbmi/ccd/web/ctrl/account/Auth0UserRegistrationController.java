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
package edu.pitt.dbmi.ccd.web.ctrl.account;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.account.UserRegistrationForm;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.AuthenticationService;
import edu.pitt.dbmi.ccd.web.service.account.Auth0UserRegistrationService;
import edu.pitt.dbmi.ccd.web.service.account.UserRegistrationService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import edu.pitt.dbmi.ccd.web.service.mail.UserRegistrationMailService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 29, 2016 3:01:22 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "auth0/user/registration")
public class Auth0UserRegistrationController implements ViewPath {

    private static final String[] TERMS_NOT_ACCEPTED = {"You must accept the terms."};
    private static final String[] REGISTRATION_FAILED = {"Registration Failed!", "Unable to register new user."};
    private static final String[] LOGIN_FAILED = {"Login Failed!", "Unable to log in."};

    private final Auth0UserRegistrationService auth0UserRegistrationService;
    private final UserRegistrationService userRegistrationService;
    private final UserRegistrationMailService userRegistrationMailService;
    private final FileManagementService fileManagementService;
    private final AuthenticationService authenticationService;
    private final AppUserService appUserService;

    @Autowired
    public Auth0UserRegistrationController(
            Auth0UserRegistrationService auth0UserRegistrationService,
            UserRegistrationService userRegistrationService,
            UserRegistrationMailService userRegistrationMailService,
            FileManagementService fileManagementService,
            AuthenticationService authenticationService,
            AppUserService appUserService) {
        this.auth0UserRegistrationService = auth0UserRegistrationService;
        this.userRegistrationService = userRegistrationService;
        this.userRegistrationMailService = userRegistrationMailService;
        this.fileManagementService = fileManagementService;
        this.authenticationService = authenticationService;
        this.appUserService = appUserService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerNewUser(
            @RequestParam("agree") boolean agree,
            @ModelAttribute("appUser") final AppUser appUser,
            final SessionStatus sessionStatus,
            final Model model,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        if (agree) {
            UserRegistrationForm userRegistrationForm = auth0UserRegistrationService.createUserRegistrationForm(appUser);
            UserAccount userAccount = userRegistrationService.registerRegularAccount(userRegistrationForm, req.getRemoteAddr(), true);
            if (userAccount == null) {
                redirAttrs.addFlashAttribute("errorMsg", REGISTRATION_FAILED);
            } else {
                userRegistrationMailService.sendUserRegistrationAlertToAdmin(userAccount);
                Subject subject = authenticationService.loginManually(userAccount, req, res);
                if (subject.isAuthenticated()) {
                    fileManagementService.setupUserHomeDirectory(userAccount);
                    redirAttrs.addFlashAttribute("appUser", appUserService.create(userAccount, true));
                    authenticationService.setLoginInfo(userAccount, req.getRemoteAddr());
                } else {
                    redirAttrs.addFlashAttribute("errorMsg", LOGIN_FAILED);
                }
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", TERMS_NOT_ACCEPTED);
            sessionStatus.setComplete();
        }

        return REDIRECT_LOGIN;
    }

}
