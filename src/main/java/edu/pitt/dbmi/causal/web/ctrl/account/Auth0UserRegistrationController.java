/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.ctrl.account;

import edu.pitt.dbmi.causal.web.ctrl.SitePaths;
import edu.pitt.dbmi.causal.web.ctrl.SiteViews;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.UserRegistrationForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.AuthService;
import edu.pitt.dbmi.causal.web.service.account.AccountRegistrationService;
import edu.pitt.dbmi.causal.web.service.file.FileManagementService;
import edu.pitt.dbmi.causal.web.service.mail.UserRegistrationMailService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class Auth0UserRegistrationController {

    private static final String REGISTRATION_ERROR = "Registration Error!";
    private static final String[] REGISTRATION_FAILED = {"Registration Failed!", "Unable to create new user at this time."};
    private static final String[] LOGIN_FAILED = {"Login Failed!", "Unable to log in during registration."};

    private final AccountRegistrationService accountRegistrationService;
    private final AuthService authService;
    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final UserRegistrationMailService userRegistrationMailService;

    @Autowired
    public Auth0UserRegistrationController(AccountRegistrationService accountRegistrationService, AuthService authService, AppUserService appUserService, FileManagementService fileManagementService, UserRegistrationMailService userRegistrationMailService) {
        this.accountRegistrationService = accountRegistrationService;
        this.authService = authService;
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.userRegistrationMailService = userRegistrationMailService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping
    public String processUserRegistration(
            @Valid @ModelAttribute("userRegistrationForm") final UserRegistrationForm userRegistrationForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            final Model model,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        // ensure form data is valid
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.userRegistrationForm", bindingResult);
            redirAttrs.addFlashAttribute("userRegistrationForm", userRegistrationForm);
            redirAttrs.addFlashAttribute("errorMsg", REGISTRATION_ERROR);

            return SitePaths.REDIRECT_AUTH0_USER_REGISTRATION;
        }

        // ensure account is created
        UserAccount userAccount = accountRegistrationService
                .registerRegularAccount(userRegistrationForm, req.getRemoteAddr(), true);
        if (userAccount == null) {
            redirAttrs.addFlashAttribute("errorMsg", REGISTRATION_FAILED);

            return SitePaths.REDIRECT_USER_REGISTRATION;
        }

        userRegistrationMailService.sendUserRegistrationAlertToAdmin(userAccount);

        Subject subject = authService.login(userAccount, req, res);
        if (subject.isAuthenticated()) {
            fileManagementService.createUserHomeDirectory(userAccount);

            AppUser appUser = appUserService
                    .create(userAccount, true, req.getRemoteAddr());
            redirAttrs.addFlashAttribute("appUser", appUser);

            return SitePaths.REDIRECT_HOME;
        } else {
            redirAttrs.addFlashAttribute("errorMsg", LOGIN_FAILED);

            return SitePaths.REDIRECT_LOGIN;
        }
    }

    @GetMapping
    public String showUserRegistration(final SessionStatus sessionStatus, final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
        } else if (currentUser.isAuthenticated()) {
            return SitePaths.REDIRECT_HOME;
        } else {
            sessionStatus.setComplete();
        }

        if (!model.containsAttribute("userRegistrationForm")) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("federated", true);

        return SiteViews.USER_REGISTRATION;
    }

}
