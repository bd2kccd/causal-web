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
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.exception.UserActivationException;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.UserService;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
public class UserRegistrationController implements ViewPath {

    private final UserAccountService userAccountService;

    private final UserService userService;

    @Autowired
    public UserRegistrationController(UserAccountService userAccountService, UserService userService) {
        this.userAccountService = userAccountService;
        this.userService = userService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String activateWebUser(
            @RequestParam(value = "account", required = true) final String account,
            final Model model) {
        String accountId = new String(Base64.getUrlDecoder().decode(account));
        UserAccount userAccount = userAccountService.findByAccountId(accountId);
        if (userAccount == null || userAccount.getActive()) {
            throw new UserActivationException();
        } else {
            userAccount.setActive(Boolean.TRUE);
            userAccountService.saveUserAccount(userAccount);

            model.addAttribute("username", userAccount.getUsername());

            return USER_ACTIVATION_SUCCESS_VIEW;
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerWebUser(
            @Value("${ccd.server.workspace}") String workspace,
            final UserRegistration userRegistration,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        String username = userRegistration.getUsername();
        if (userAccountService.findByUsername(username) == null) {
            String email = userRegistration.getEmail();
            if (userAccountService.findUserAccountByEmail(email) == null) {
                if (userService.registerNewUser(userRegistration, request.getRequestURL().toString())) {
                    String msg = "Thank you for registering.  Check your email to verify and activate your account.";
                    redirectAttributes.addFlashAttribute("successMsg", msg);
                } else {
                    redirectAttributes.addFlashAttribute("errorMsg", String.format("Unable to register user '%s' at this time.", username));
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", String.format("This email '%s' has already been registered.", email));
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", String.format("Username '%s' is already taken.", username));
        }

        return REDIRECT_LOGIN;
    }

}
