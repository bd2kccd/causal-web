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
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.LOGIN;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Aug 5, 2015 1:54:06 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
/**
 *
 * May 14, 2015 12:39:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class ApplicationController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;

    private final boolean webapp;

    @Autowired(required = true)
    public ApplicationController(UserAccountService userAccountService, AppUserService appUserService, boolean webapp) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
        this.webapp = webapp;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage(final Model model) {
        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(
            @Value("${ccd.desktop.default.pwd:password123}") final String defaultPassword,
            @Value("${ccd.desktop.set.error:Unable to setup initial settings.}") final String signInErrMsg,
            final SessionStatus sessionStatus,
            final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
            if (webapp) {
                return LOGIN_VIEW;
            } else {
                return REDIRECT_SETUP;
            }
        } else {
            if (currentUser.isAuthenticated()) {
                return REDIRECT_HOME;
            } else {
                if (webapp) {
                    return LOGIN_VIEW;
                } else {
                    String username = System.getProperty("user.name");
                    UserAccount userAccount = userAccountService.findByUsername(username);
                    if (userAccount == null) {
                        return REDIRECT_SETUP;
                    }

                    UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), defaultPassword);
                    token.setRememberMe(true);
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
                    userAccountService.saveUserAccount(userAccount);

                    model.addAttribute("appUser", appUserService.createAppUser(userAccount));

                    userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
                    userAccountService.saveUserAccount(userAccount);

                    return REDIRECT_HOME;
                }
            }
        }
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(final UsernamePasswordToken credentials, final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        String username = credentials.getUsername();
        try {
            currentUser.login(credentials);
        } catch (AuthenticationException exception) {
            LOGGER.warn(String.format("Failed login attempt from user %s.", username), exception);
            model.addAttribute("errorMsg", "Invalid username and/or password.");
            return LOGIN_VIEW;
        }

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount.getActive()) {
            model.addAttribute("appUser", appUserService.createAppUser(userAccount));

            userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
            userAccountService.saveUserAccount(userAccount);

            return REDIRECT_HOME;
        } else {
            currentUser.logout();
            model.addAttribute("errorMsg", "Your account has not been activated.");

            return LOGIN_VIEW;
        }
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String goHome(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return HOME_VIEW;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logOut(final SessionStatus sessionStatus, final RedirectAttributes redirectAttributes) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMsg", "You Have Successfully Logged Out.");
        }

        return REDIRECT_LOGIN;
    }

}
