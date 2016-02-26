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

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.exception.UserActivationException;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.user.UserService;
import java.util.Base64;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.subject.WebSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
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

    private static final String[] REGISTRATION_SUCCESS = {
        "Thank you for registering!",
        "You should receive an email from us shortly."
    };

    private final UserAccountService userAccountService;

    private final UserService userService;

    private final AppUserService appUserService;

    @Autowired
    public UserRegistrationController(UserAccountService userAccountService, UserService userService, AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.userService = userService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = "federated", method = RequestMethod.POST)
    public String processTermsAndConditions(
            @RequestParam("agree") boolean agree,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final SessionStatus sessionStatus,
            final RedirectAttributes redirectAttributes,
            final Model model) {
        if (agree) {
            if (userService.registerNewFederatedUser(appUser, request)) {
                String username = appUser.getEmail();
                UserAccount userAccount = userAccountService.findByUsername(username);
                model.addAttribute("appUser", appUserService.createAppUser(userAccount, false));

                new WebSubject.Builder(request, response)
                        .authenticated(true)
                        .sessionCreationEnabled(true)
                        .buildSubject();
                return REDIRECT_HOME;
            } else {
                sessionStatus.setComplete();
                redirectAttributes.addFlashAttribute("errorMsg", Collections.singletonList("Sorry, registration is unavailable at this time."));
                return REDIRECT_LOGIN;
            }
        } else {
            return REDIRECT_TERMS;
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerWebUser(
            final UserRegistration userRegistration,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        if (userRegistration.isAgree()) {
            String username = userRegistration.getUsername();
            if (userAccountService.findByUsername(username) == null) {
                if (userService.registerNewUser(userRegistration, request)) {
                    redirectAttributes.addFlashAttribute("successMsg", REGISTRATION_SUCCESS);
                } else {
                    redirectAttributes.addFlashAttribute("errorMsg", Collections.singletonList("Sorry, registration is unavailable at this time."));
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", Collections.singletonList(username + " has already been registered."));
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", Collections.singletonList("You must accept the terms and conditions."));
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String activateNewUser(
            @RequestParam(value = "account", required = true) final String account,
            final Model model) {
        String accountId = new String(Base64.getUrlDecoder().decode(account));
        UserAccount userAccount = userAccountService.findByAccount(accountId);
        if (userAccount == null || userAccount.isActive()) {
            throw new UserActivationException();
        } else {
            userAccount.setActive(Boolean.TRUE);
            userAccountService.saveUserAccount(userAccount);

            model.addAttribute("username", userAccount.getUsername());

            return USER_ACTIVATION_SUCCESS_VIEW;
        }
    }

}
