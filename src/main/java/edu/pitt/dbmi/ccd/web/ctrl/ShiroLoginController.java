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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Feb 18, 2016 1:37:14 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@Profile("shiro")
@SessionAttributes("appUser")
public class ShiroLoginController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroLoginController.class);

    private static final String LOGIN_VIEW = "shiroLogin";

    private final UserAccountService userAccountService;
    private final AppUserService appUserService;

    @Autowired
    public ShiroLoginController(UserAccountService userAccountService, AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(final SessionStatus sessionStatus, final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
        } else if (currentUser.isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            sessionStatus.setComplete();
        }

        model.addAttribute("userRegistration", new UserRegistration());

        return LOGIN_VIEW;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(
            final UsernamePasswordToken credentials,
            final RedirectAttributes redirectAttributes) {
        Subject currentUser = SecurityUtils.getSubject();
        String username = credentials.getUsername();
        try {
            currentUser.login(credentials);
        } catch (AuthenticationException exception) {
            LOGGER.warn(String.format("Failed login attempt from user %s.", username));
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid username and/or password.");
            return REDIRECT_LOGIN;
        }

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount.isActive()) {
            return REDIRECT_HOME;
        } else {
            currentUser.logout();
            redirectAttributes.addFlashAttribute("errorMsg", "Your account has not been activated.");
            return REDIRECT_LOGIN;
        }
    }

}
