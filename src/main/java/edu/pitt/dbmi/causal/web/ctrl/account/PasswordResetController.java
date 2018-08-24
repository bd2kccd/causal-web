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
import edu.pitt.dbmi.causal.web.model.account.PasswordReset;
import edu.pitt.dbmi.causal.web.model.account.PasswordResetRequestForm;
import edu.pitt.dbmi.causal.web.service.account.PasswordResetService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class PasswordResetController {

    private static final String[] UNACTIVATED_ACCOUNT = {"Password Reset Failed!", "Account has not been activated."};
    private static final String[] UNSUCCESSFUL_REQUEST = {"Password Reset Request Failed!", "Unable to proccess password reset request."};
    private static final String[] SUCCESSFUL_REQUEST = {"Password Reset Request Success!", "Please check your email for information on resetting your password."};
    private static final String[] PASSWORD_RESET_FAILED = {"Password Reset Failed!", "Unable to reset password."};
    private static final String[] PASSWORD_RESET_SUCCESS = {"Password Reset Success!", "You have successfully reset your password."};

    private final UserAccountService userAccountService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public PasswordResetController(UserAccountService userAccountService, PasswordResetService passwordResetService) {
        this.userAccountService = userAccountService;
        this.passwordResetService = passwordResetService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping("form")
    public String processPasswordResetForm(
            @Valid @ModelAttribute("passwordReset") final PasswordReset passwordReset,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordReset", bindingResult);
            redirectAttributes.addFlashAttribute("passwordReset", passwordReset);

            return "redirect:/user/account/password/reset/form?reset=" + passwordReset.getActivationKey();
        }

        String activationKey = new String(Base64.getUrlDecoder().decode(passwordReset.getActivationKey()));
        UserAccount userAccount = userAccountService.getRepository()
                .findByActivationKey(activationKey);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        if (passwordResetService.proccessPasswordResetRequestForm(userAccount, passwordReset.getPassword())) {
            redirectAttributes.addFlashAttribute("successMsg", PASSWORD_RESET_SUCCESS);

            return SitePaths.REDIRECT_LOGIN;
        } else {
            redirectAttributes.addFlashAttribute("passwordReset", passwordReset);
            redirectAttributes.addFlashAttribute("errorMsg", PASSWORD_RESET_FAILED);

            return "redirect:/user/account/password/reset/form?reset=" + passwordReset.getActivationKey();
        }
    }

    @GetMapping("form")
    public String showUserPasswordResetForm(
            @RequestParam(value = "reset", required = true) final String resetKey,
            final HttpServletRequest request,
            final Model model) {
        String activationKey = new String(Base64.getUrlDecoder().decode(resetKey));
        if (userAccountService.getRepository().existsByActivationKey(activationKey)) {
            if (!model.containsAttribute("passwordReset")) {
                model.addAttribute("passwordReset", new PasswordReset(resetKey));
            }
        } else {
            throw new ResourceNotFoundException();
        }

        return SiteViews.USER_PASSWORD_RESET;
    }

    @GetMapping
    public String showPasswordResetPage(final Model model) {
        if (!model.containsAttribute("resetRequestForm")) {
            model.addAttribute("resetRequestForm", new PasswordResetRequestForm());
        }

        return SiteViews.PASSWORD_RESET_REQUEST;
    }

    @PostMapping
    public String processPasswordReset(
            @Valid @ModelAttribute("passwordRecovery") final PasswordResetRequestForm passwordRecovery,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordRecovery", bindingResult);
            redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);

            return SitePaths.REDIRECT_PASSWORD_RESET_REQUEST;
        }

        String email = passwordRecovery.getEmail();
        UserAccount userAccount = userAccountService.getRepository()
                .findByUsername(email);

        if (userAccount != null) {
            // ensure account is activated
            if (!userAccount.isActivated()) {
                redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);
                redirectAttributes.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);

                return SitePaths.REDIRECT_PASSWORD_RESET_REQUEST;
            }

            if (!passwordResetService.proccessPasswordResetRequest(userAccount, request)) {
                redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);
                redirectAttributes.addFlashAttribute("errorMsg", UNSUCCESSFUL_REQUEST);

                return SitePaths.REDIRECT_PASSWORD_RESET_REQUEST;
            }
        }

        // return success even if user account does not exist
        redirectAttributes.addFlashAttribute("successMsg", SUCCESSFUL_REQUEST);

        return SitePaths.REDIRECT_LOGIN;
    }

}
