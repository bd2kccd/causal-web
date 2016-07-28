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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.LoginCredentials;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.net.UnknownHostException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * May 27, 2016 3:04:12 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class ShiroLoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroLoginService.class);

    public static final String INVALID_CREDENTIALS = "Invalid username and/or password.";
    public static final String UNACTIVATED_ACCOUNT = "Your account has not been activated.";
    public static final String LOGOUT_SUCCESS = "You Have Successfully Logged Out.";

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;

    @Autowired
    public ShiroLoginService(UserAccountService userAccountService, AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
    }

    public void logoutUser(
            SessionStatus sessionStatus,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMsg", LOGOUT_SUCCESS);
        }

        if (request.getSession() != null) {
            request.getSession().invalidate();
        }
    }

    public boolean loginUser(
            final LoginCredentials loginCredentials,
            final RedirectAttributes redirectAttributes,
            final Model model,
            final HttpServletRequest request) {
        String username = loginCredentials.getLoginUsername();
        String password = loginCredentials.getLoginPassword();
        boolean rememberMe = loginCredentials.isRememberMe();

        // shiro login
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(new UsernamePasswordToken(username, password, rememberMe));
        } catch (AuthenticationException exception) {
            LOGGER.warn(String.format("Failed login attempt from user %s.", username));
            redirectAttributes.addFlashAttribute("errorMsg", INVALID_CREDENTIALS);
            redirectAttributes.addFlashAttribute("loginCredentials", loginCredentials);

            return false;
        }

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount.isActive()) {
            UserLogin userLogin = userAccount.getUserLogin();
            userLogin.setLastLoginDate(userLogin.getLoginDate());
            userLogin.setLastLoginLocation(userLogin.getLoginLocation());
            userLogin.setLoginDate(new Date(System.currentTimeMillis()));
            try {
                userLogin.setLoginLocation(UriTool.InetNTOA(request.getRemoteAddr()));
            } catch (UnknownHostException exception) {
                LOGGER.info(exception.getLocalizedMessage());
            }
            // remove any previous activation request
            userAccount.setActivationKey(null);

            userAccountService.saveUserAccount(userAccount);

            model.addAttribute("appUser", appUserService.createAppUser(userAccount, false));
        } else {
            currentUser.logout();
            redirectAttributes.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);

            return false;
        }

        return true;
    }

}
