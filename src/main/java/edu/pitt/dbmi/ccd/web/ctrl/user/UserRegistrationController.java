/*
 * Copyright (C) 2015 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.ctrl.user;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.UserActivationException;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.model.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.UserService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Aug 4, 2015 12:36:43 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "user/registration")
public class UserRegistrationController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationController.class);

    private final UserAccountService userAccountService;

    private final UserService userService;

    @Autowired(required = true)
    public UserRegistrationController(
            UserAccountService userAccountService,
            UserService userService) {
        this.userAccountService = userAccountService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerNewUser(
            @Value("${app.server.workspace}") String workspace,
            final UserRegistration userRegistration,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        String username = userRegistration.getUsername();
        if (userAccountService.findByUsername(username) == null) {
            try {
                userService.registerNewUser(userRegistration, workspace, request.getRequestURL().toString());
            } catch (Exception exception) {
                LOGGER.warn(
                        String.format("Unable to register new user account for %s.", username),
                        exception);
                redirectAttributes.addFlashAttribute("errorMsg", String.format("Unable to create account for '%s'.", username));
            }

            String msg = "Thank you for registering."
                    + "Check your email to verify and activate your account.";
            redirectAttributes.addFlashAttribute("successMsg", msg);
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", String.format("Username '%s' is already taken.", username));
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String userActivation(
            @RequestParam(value = "user", required = true) final String user,
            @RequestParam(value = "key", required = true) final String activationKey,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsernameAndActivationKey(user, activationKey);
        if (userAccount == null) {
            throw new UserActivationException();
        } else {
            userAccount.setActive(Boolean.TRUE);
            userAccount.setActivationKey(null);
            userAccountService.saveUserAccount(userAccount);

            model.addAttribute("username", userAccount.getUsername());

            return "user/userActivationSuccess";
        }
    }

}
