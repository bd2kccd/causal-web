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
package edu.pitt.dbmi.causal.web.ctrl.account;

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.account.PasswordResetForm;
import edu.pitt.dbmi.causal.web.model.account.PasswordResetRequestForm;
import edu.pitt.dbmi.causal.web.model.template.Message;
import edu.pitt.dbmi.causal.web.service.account.PasswordResetService;
import edu.pitt.dbmi.causal.web.service.mail.PasswordResetMailService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
 * Aug 16, 2017 3:19:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "user/account/password/reset")
public class PasswordResetController implements ViewPath {

    private final String ACCOUNT_NOT_FOUND = "Account not found.";
    private final String PASSWORD_RESET_ERROR = "Password reset error!";
    private static final String[] ACCOUNT_NOT_ACTIVATED = {"Password Rest Failed!", "Account has not been activated."};
    private static final String[] UNABLE_TO_RESET_PASSWORD = new String[]{"Password Rest Failed!", "Unable to send password reset request."};
    private static final String[] PASSWORD_RESET_REQUEST_SUCCESS = {"Password reset requested.", "Please check your email."};
    private static final List<String> PASSWORD_RESET_SUCCESS = Collections.singletonList("Your password has been successfully changed.");
    private static final List<String> PASSWORD_RESET_FAIL = Collections.singletonList("Sorry, we are unable to reset your password at this time.");

    private final PasswordResetService passwordResetService;
    private final PasswordResetMailService passwordResetMailService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService, PasswordResetMailService passwordResetMailService) {
        this.passwordResetService = passwordResetService;
        this.passwordResetMailService = passwordResetMailService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processPasswordReset(
            @Valid @ModelAttribute("passwordResetForm") final PasswordResetForm passwordResetForm,
            final BindingResult bindingResult,
            @RequestParam(value = "activation", required = true) final String activation,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.passwordResetForm", bindingResult);
            redirAttrs.addFlashAttribute("passwordResetForm", passwordResetForm);

            return REDIRECT_PASSWORD_RESET;
        }

        Message message = new Message("Causal Web: Password Reset");
        if (passwordResetService.resetPassword(activation, passwordResetForm)) {
            message.setSuccess(true);
            message.setMessageTitle("Password Reset Success!");
            message.setMessages(PASSWORD_RESET_SUCCESS);
        } else {
            message.setMessageTitle("Password Reset Failed!");
            message.setMessages(PASSWORD_RESET_FAIL);
        }
        redirAttrs.addFlashAttribute("message", message);

        return REDIRECT_MESSAGE;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showPasswordReset(
            @RequestParam(value = "activation", required = true) final String activation,
            final Model model) {
        if (passwordResetService.isValidActivationKey(activation)) {
            if (!model.containsAttribute("passwordResetForm")) {
                model.addAttribute("passwordResetForm", new PasswordResetForm());
            }
        } else {
            throw new ResourceNotFoundException();
        }

        return PASSWORD_RESET_VIEW;
    }

    @RequestMapping(value = "request", method = RequestMethod.POST)
    public String processPasswordResetRequest(
            @Valid @ModelAttribute("passwordResetRequestForm") final PasswordResetRequestForm passwordResetRequestForm,
            final BindingResult bindingResult,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req) {
        // ensure form data is valid
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.passwordResetRequestForm", bindingResult);
            redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
            redirAttrs.addFlashAttribute("errorMsg", PASSWORD_RESET_ERROR);

            return REDIRECT_PASSWORD_RESET_REQUEST;
        }

        // ensure account exists
        UserAccount userAccount = passwordResetService.retrieveUserAccount(passwordResetRequestForm);
        if (userAccount == null) {
            bindingResult.rejectValue("email", "passwordResetRequestForm.email", ACCOUNT_NOT_FOUND);
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.passwordResetRequestForm", bindingResult);
            redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
            redirAttrs.addFlashAttribute("errorMsg", PASSWORD_RESET_ERROR);

            return REDIRECT_PASSWORD_RESET_REQUEST;
        }

        // ensure account is activated
        if (!userAccount.isActivated()) {
            redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
            redirAttrs.addFlashAttribute("errorMsg", ACCOUNT_NOT_ACTIVATED);

            return REDIRECT_PASSWORD_RESET_REQUEST;
        }

        // ensure request link is created
        URI uri = passwordResetService.createPasswordResetLink(userAccount, req);
        if (uri == null) {
            redirAttrs.addFlashAttribute("passwordResetRequestForm", passwordResetRequestForm);
            redirAttrs.addFlashAttribute("errorMsg", UNABLE_TO_RESET_PASSWORD);

            return REDIRECT_PASSWORD_RESET_REQUEST;
        }

        passwordResetMailService.sendPasswordResetLinkToUser(userAccount, uri);
        redirAttrs.addFlashAttribute("successMsg", PASSWORD_RESET_REQUEST_SUCCESS);

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "request", method = RequestMethod.GET)
    public String showPasswordResetRequest(final Model model) {
        if (!model.containsAttribute("passwordResetRequestForm")) {
            PasswordResetRequestForm form = new PasswordResetRequestForm();
            form.setEmail("kvb2@pitt.edu");
            model.addAttribute("passwordResetRequestForm", form);
        }

        return PASSWORD_RESET_REQUEST_VIEW;
    }

}
