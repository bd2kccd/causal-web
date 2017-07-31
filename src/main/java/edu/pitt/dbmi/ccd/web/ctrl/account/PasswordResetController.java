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
import edu.pitt.dbmi.ccd.web.model.account.PasswordResetForm;
import edu.pitt.dbmi.ccd.web.model.account.PasswordResetRequestForm;
import edu.pitt.dbmi.ccd.web.model.template.MessageTemplateData;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.account.PasswordResetService;
import edu.pitt.dbmi.ccd.web.service.mail.PasswordResetMailService;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 6, 2016 12:54:11 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "user/account/password/reset")
public class PasswordResetController implements ViewPath {

    private static final String[] UNACTIVATED_ACCOUNT = {"Password Rest Failed!", "Your account has not been activated."};
    private static final String[] USER_NOT_FOUND = {"Password Reset Failed!", "No such account found."};
    private static final List<String> PASSWORD_RESET_SUCCESS = Collections.singletonList("Your password has been successfully changed.");
    private static final List<String> PASSWORD_RESET_FAIL = Collections.singletonList("Sorry, we are unable to reset your password at this time.");

    private final PasswordResetService passwordResetService;
    private final PasswordResetMailService passwordResetMailService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService, PasswordResetMailService passwordResetMailService) {
        this.passwordResetService = passwordResetService;
        this.passwordResetMailService = passwordResetMailService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showResetPasswordForm(
            @RequestParam(value = "activation", required = true) final String activation,
            final Model model) {
        if (passwordResetService.isValidActivationKey(activation)) {
            if (!model.containsAttribute("passwordResetForm")) {
                model.addAttribute("passwordResetForm", new PasswordResetForm(activation));
            }
        } else {
            throw new ResourceNotFoundException();
        }

        return PASSWORD_RESET_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processPasswordReset(
            @Valid @ModelAttribute("passwordResetForm") final PasswordResetForm passwordResetForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.passwordResetForm", bindingResult);
            redirAttrs.addFlashAttribute("passwordResetForm", passwordResetForm);
        } else {
            MessageTemplateData templateData = new MessageTemplateData("Causal Web: Password Reset");
            if (passwordResetService.changePassword(passwordResetForm)) {
                templateData.setSuccess(true);
                templateData.setMessageTitle("Password Reset Success!");
                templateData.setMessages(PASSWORD_RESET_SUCCESS);
            } else {
                templateData.setMessageTitle("Password Reset Failed!");
                templateData.setMessages(PASSWORD_RESET_FAIL);
            }
            redirAttrs.addFlashAttribute("message", templateData);
        }

        return REDIRECT_MESSAGE;
    }

    @RequestMapping(value = "request", method = RequestMethod.POST)
    public String processPasswordRestRequest(
            @Valid @ModelAttribute("passwordResetRequestForm") final PasswordResetRequestForm passwordResetRequestForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.passwordResetRequestForm", bindingResult);
            redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
        } else {
            UserAccount userAccount = passwordResetService.retrieveUserAccount(passwordResetRequestForm);
            if (userAccount == null) {
                redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
                redirAttrs.addFlashAttribute("errorMsg", USER_NOT_FOUND);
            } else {
                if (userAccount.isActivated()) {
                    URI uri = passwordResetService.createPasswordResetLink(userAccount, req);
                    if (uri == null) {
                        redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
                        redirAttrs.addFlashAttribute("errorMsg", new String[]{"Unable to send password reset request."});
                    } else {
                        passwordResetMailService.sendPasswordResetLinkToUser(userAccount, uri);
                        redirAttrs.addFlashAttribute("successMsg", new String[]{"Please check your email to reset password."});
                    }
                } else {
                    redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
                    redirAttrs.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);
                }
            }
        }

        return REDIRECT_LOGIN;
    }

}
