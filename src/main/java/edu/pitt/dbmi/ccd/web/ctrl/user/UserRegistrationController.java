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
package edu.pitt.dbmi.ccd.web.ctrl.user;

import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.user.UserService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Feb 23, 2016 5:09:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "user/registration")
public class UserRegistrationController implements ViewPath {

    private final UserAccountService userAccountService;

    private final UserService userService;

    @Autowired
    public UserRegistrationController(UserAccountService userAccountService, UserService userService) {
        this.userAccountService = userAccountService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerWebUser(
            final UserRegistration userRegistration,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        if (userRegistration.isAgree()) {
            String username = userRegistration.getUsername();
            if (userAccountService.findByUsername(username) == null) {
                if (userService.registerNewUser(userRegistration, request.getRemoteAddr())) {
                    String msg = "Thank You! Please check your email to activate your account.";
                    redirectAttributes.addFlashAttribute("successMsg", msg);
                } else {
                    redirectAttributes.addFlashAttribute("errorMsg", "Sorry, registration is unavailable at this time.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", username + " has already been registered.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "You must accept the terms and conditions.");
        }

        return REDIRECT_LOGIN;
    }

}
