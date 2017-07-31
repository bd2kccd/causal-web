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
import edu.pitt.dbmi.ccd.db.service.UserEventLogService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.account.UserRegistrationForm;
import edu.pitt.dbmi.ccd.web.model.template.MessageTemplateData;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.AuthenticationService;
import edu.pitt.dbmi.ccd.web.service.account.UserRegistrationService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import edu.pitt.dbmi.ccd.web.service.mail.UserRegistrationMailService;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 4, 2016 2:48:15 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "/user/account/registration")
public class UserRegistrationController implements ViewPath {

    private static final String[] LOGIN_FAILED = {"Login Failed!", "Unable to log in."};
    private static final String[] REGISTRATION_SUCCESS = {"Registration Success!", "Check your email to activate your account."};
    private static final String[] REGISTRATION_FAILED = {"Registration Failed!", "Unable to register new user."};
    private static final String[] USERNAME_EXISTED = {"Registration Failed!", "This account already exists."};

    private final UserRegistrationService userRegistrationService;
    private final UserRegistrationMailService userRegistrationMailService;
    private final AuthenticationService authenticationService;
    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final UserEventLogService userEventLogService;

    @Autowired
    public UserRegistrationController(
            UserRegistrationService userRegistrationService,
            UserRegistrationMailService userRegistrationMailService,
            AuthenticationService authenticationService,
            AppUserService appUserService,
            FileManagementService fileManagementService,
            UserEventLogService userEventLogService) {
        this.userRegistrationService = userRegistrationService;
        this.userRegistrationMailService = userRegistrationMailService;
        this.authenticationService = authenticationService;
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.userEventLogService = userEventLogService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String activateNewUser(
            @RequestParam(value = "activation", required = true) final String activation,
            final RedirectAttributes redirAttrs) {
        UserAccount userAccount = userRegistrationService.findUserAccount(activation);
        if (userAccount == null || userAccount.isActivated()) {
            throw new ResourceNotFoundException();
        } else {
            MessageTemplateData templateData = new MessageTemplateData("Causal Web: User Activation");
            if (userRegistrationService.activateUserAccount(userAccount)) {
                templateData.setSuccess(true);
                templateData.setMessageTitle("User Activation Success!");
                templateData.setMessages(Collections.singletonList(String.format("You have successfully activated user '%s'.", userAccount.getUsername())));
            } else {
                templateData.setMessageTitle("User Activation Failed!");
                templateData.setMessages(Collections.singletonList(String.format("Unable to activate user '%s'.", userAccount.getUsername())));
            }
            redirAttrs.addFlashAttribute("message", templateData);
        }

        return REDIRECT_MESSAGE;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processUserRegistration(
            @Valid @ModelAttribute("userRegistrationForm") final UserRegistrationForm userRegistrationForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            final Model model,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.userRegistrationForm", bindingResult);
            redirAttrs.addFlashAttribute("userRegistrationForm", userRegistrationForm);
            redirAttrs.addFlashAttribute("errorMsg", "Registration failed!");
        } else {
            if (userRegistrationService.accountExists(userRegistrationForm)) {
                redirAttrs.addFlashAttribute("userRegistrationForm", userRegistrationForm);
                redirAttrs.addFlashAttribute("errorMsg", USERNAME_EXISTED);
            } else {
                UserAccount userAccount = userRegistrationService.registerRegularAccount(userRegistrationForm, req.getRemoteAddr(), false);
                if (userAccount == null) {
                    redirAttrs.addFlashAttribute("errorMsg", REGISTRATION_FAILED);
                } else {
                    userEventLogService.logUserRegistration(userAccount);
                    userRegistrationMailService.sendUserRegistrationAlertToAdmin(userAccount);
                    if (userAccount.isActivated()) {
                        Subject subject = authenticationService.loginManually(userAccount, req, res);
                        if (subject.isAuthenticated()) {
                            fileManagementService.setupUserHomeDirectory(userAccount);
                            redirAttrs.addFlashAttribute("appUser", appUserService.create(userAccount, false));
                            authenticationService.setLoginInfo(userAccount, req.getRemoteAddr());
                        } else {
                            redirAttrs.addFlashAttribute("errorMsg", LOGIN_FAILED);
                        }
                    } else {
                        // send e-mail notification to user
                        userRegistrationMailService.sendAccountActivationToUser(userAccount, createActivationLink(userAccount, req));
                        redirAttrs.addFlashAttribute("successMsg", REGISTRATION_SUCCESS);
                    }
                }
            }
        }

        return REDIRECT_LOGIN;
    }

    protected URI createActivationLink(UserAccount userAccount, HttpServletRequest req) {
        String actionKey = userAccount.getActionKey();

        return UriTool.buildURI(req)
                .pathSegment("user", "account", "registration", "activate")
                .queryParam("activation", Base64.getUrlEncoder().encodeToString(actionKey.getBytes()))
                .build().toUri();
    }

}
