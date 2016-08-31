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
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserLoginService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.LoginCredentials;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
 * Aug 22, 2016 7:21:13 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    public static final String INVALID_CREDENTIALS = "Invalid username and/or password.";
    public static final String UNACTIVATED_ACCOUNT = "Your account has not been activated.";
    public static final String LOGOUT_SUCCESS = "You Have Successfully Logged Out.";

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;
    private final EventLogService eventLogService;
    private final FileManagementService fileManagementService;
    private final UserLoginService userLoginService;

    @Autowired
    public ApplicationService(UserAccountService userAccountService, AppUserService appUserService, EventLogService eventLogService, FileManagementService fileManagementService, UserLoginService userLoginService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
        this.eventLogService = eventLogService;
        this.fileManagementService = fileManagementService;
        this.userLoginService = userLoginService;
    }

    public void retrieveFileCounts(final AppUser appUser, final Model model) {
        fileManagementService.retrieveFileCountSummary(appUser, model);
    }

    public boolean logInUser(
            LoginCredentials loginCredentials,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request) {
        String username = loginCredentials.getLoginUsername();
        String password = loginCredentials.getLoginPassword();
        boolean rememberMe = loginCredentials.isRememberMe();

        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(new UsernamePasswordToken(username, password, rememberMe));
        } catch (AuthenticationException exception) {
            LOGGER.info(String.format("Failed login attempt from user %s.", username));
        }

        if (!currentUser.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMsg", INVALID_CREDENTIALS);
            redirectAttributes.addFlashAttribute("loginCredentials", loginCredentials);
            eventLogService.logUserSignInFailed(loginCredentials.getLoginUsername(), UriTool.getInetNTOA(request.getRemoteAddr()));

            return false;
        }

        UserAccount userAccount = userAccountService.findByUsername(loginCredentials.getLoginUsername());
        if (!userAccount.isActivated()) {
            currentUser.logout();
            redirectAttributes.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);

            return false;
        }

        Long location = UriTool.getInetNTOA(request.getRemoteAddr());
        eventLogService.logUserSignIn(userAccount, location);
        userLoginService.logUserSignIn(userAccount, location);

        // reset data after successful login
        userAccount.setActivationKey(null);
        userAccount.getUserLoginAttempts().clear();
        userAccountService.save(userAccount);

        // create user directories if not existed
        fileManagementService.createUserDirectories(userAccount);

        model.addAttribute("appUser", appUserService.createAppUser(userAccount, false));

        return true;
    }

    public void logOutUser(
            AppUser appUser,
            SessionStatus sessionStatus,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMsg", LOGOUT_SUCCESS);

            String username = appUser.getUsername();
            UserAccount userAccount = userAccountService.findByUsername(username);
            eventLogService.logUserSignOut(userAccount, UriTool.getInetNTOA(request.getRemoteAddr()));
        }

        HttpSession httpSession = request.getSession();
        if (httpSession != null) {
            httpSession.invalidate();
        }
    }

}
