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
package edu.pitt.dbmi.ccd.web.ctrl.account;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.PasswordRecovery;
import edu.pitt.dbmi.ccd.web.domain.account.PasswordReset;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.account.AccountRecoveryService;
import java.util.Base64;
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
 * May 30, 2016 3:34:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "user/account/recovery")
public class AccountRecoveryController implements ViewPath {

    private final AccountRecoveryService accountRecoveryService;

    @Autowired
    public AccountRecoveryController(AccountRecoveryService accountRecoveryService) {
        this.accountRecoveryService = accountRecoveryService;
    }

    @RequestMapping(value = "password/request", method = RequestMethod.POST)
    public String passwordRecovery(
            @Valid @ModelAttribute("passwordRecovery") final PasswordRecovery passwordRecovery,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordRecovery", bindingResult);
            redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);
        } else {
            accountRecoveryService.recoverPasswordRequest(passwordRecovery, redirectAttributes, request);
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "password", method = RequestMethod.GET)
    public String showPasswordResetPage(
            @RequestParam(value = "reset", required = true) final String resetKey,
            final HttpServletRequest request,
            final Model model) {
        String activationKey = new String(Base64.getUrlDecoder().decode(resetKey));
        if (accountRecoveryService.isValidActivationKey(activationKey)) {
            if (!model.containsAttribute("passwordReset")) {
                model.addAttribute("passwordReset", new PasswordReset(activationKey));
            }
        } else {
            throw new ResourceNotFoundException();
        }

        return USER_PASSWORD_RESET_VIEW;
    }

    @RequestMapping(value = "password", method = RequestMethod.POST)
    public String showPasswordResetPage(
            @Valid @ModelAttribute("passwordReset") final PasswordReset passwordReset,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordReset", bindingResult);
            redirectAttributes.addFlashAttribute("passwordReset", passwordReset);

            String activationKey = passwordReset.getActivationKey();
            return "redirect:/user/recovery/password?reset=" + Base64.getUrlEncoder().encodeToString(activationKey.getBytes());
        }

        accountRecoveryService.resetPassword(passwordReset, redirectAttributes);

        return REDIRECT_MESSAGE;
    }

}
