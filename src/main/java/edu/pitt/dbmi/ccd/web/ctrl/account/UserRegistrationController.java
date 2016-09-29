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
import edu.pitt.dbmi.ccd.web.domain.account.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.account.UserRegistrationService;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * Feb 23, 2016 5:09:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "/user/account/registration")
public class UserRegistrationController implements ViewPath {

    private final UserRegistrationService userRegistrationService;

    @Autowired
    public UserRegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerNewRegularUser(
            @Valid @ModelAttribute("userRegistration") final UserRegistration userRegistration,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final Model model,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistration", bindingResult);
            redirectAttributes.addFlashAttribute("userRegistration", userRegistration);
            redirectAttributes.addFlashAttribute("errorMsg", "Registration failed!");
        } else {
            userRegistrationService.registerNewRegularUser(userRegistration, false, model, redirectAttributes, req, res);
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String activateNewUser(
            @RequestParam(value = "activation", required = true) final String activation,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        String activationKey = new String(Base64.getUrlDecoder().decode(activation));
        userRegistrationService.activateNewUser(activationKey, request, redirectAttributes);

        return REDIRECT_MESSAGE;
    }

}
