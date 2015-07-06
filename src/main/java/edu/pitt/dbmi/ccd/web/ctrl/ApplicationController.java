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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.util.Date;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 *
 * May 14, 2015 12:39:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class ApplicationController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;

    @Autowired(required = true)
    public ApplicationController(
            UserAccountService userAccountService,
            AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String goHome(@ModelAttribute("appUser") AppUser appUser, Model model) {
        String userFullName = appUser.getFirstName() + " " + appUser.getLastName();

        model.addAttribute("userFullName", userFullName);
        model.addAttribute("lastLogin", FileUtility.DATE_FORMAT.format(appUser.getLastLoginDate()));

        return HOME;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String processLoginFromLogout(
            final UsernamePasswordToken credentials,
            final Model model) {
        return processLogin(credentials, model);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logOut(Model model, SessionStatus sessionStatus) {
        String url;

        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();
            model.addAttribute("successMsg", "You Have Successfully Logged Out.");
            url = LOGIN;
        } else {
            url = REDIRECT_LOGIN;
        }

        return url;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(
            final UsernamePasswordToken credentials,
            final Model model) {

        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(credentials);
        } catch (AuthenticationException exception) {
            LOGGER.warn(
                    String.format("Failed login attempt from user %s.", credentials.getUsername()),
                    exception);
            model.addAttribute("errorMsg", "Invalid username and/or password.");
            return LOGIN;
        }

        String username = (String) currentUser.getPrincipal();
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount.isActive()) {
            model.addAttribute("appUser", appUserService.createAppUser(userAccount));
            return REDIRECT_HOME;
        } else {
            currentUser.logout();
            model.addAttribute("errorMsg", "Your account has not been activated.");

            return LOGIN;
        }
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(
            @Value("${app.webapp:true}") final boolean isWebApplication,
            @Value("${app.default.pwd:password123}") final String defaultPassword,
            @Value("${app.login.error:Unable to setup initial settings.}") final String signInErrMsg,
            final Model model) {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            if (isWebApplication) {
                return LOGIN;
            } else {
                String username = System.getProperty("user.name");
                UserAccount userAccount = userAccountService.findByUsername(username);
                if (userAccount == null) {
                    return REDIRECT_SETUP;
                }

                UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), defaultPassword);
                token.setRememberMe(true);
                Subject currentUser = SecurityUtils.getSubject();
                try {
                    currentUser.login(token);
                } catch (AuthenticationException exception) {
                    LOGGER.warn(
                            String.format("Failed login attempt from user %s.", token.getUsername()),
                            exception);
                    model.addAttribute("errorMsg", signInErrMsg);
                    return REDIRECT_SETUP;
                }

                userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
                userAccountService.save(userAccount);

                model.addAttribute("appUser", appUserService.createAppUser(userAccount));

                return REDIRECT_HOME;
            }
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage(final Model model) {
        return REDIRECT_LOGIN;
    }

}
