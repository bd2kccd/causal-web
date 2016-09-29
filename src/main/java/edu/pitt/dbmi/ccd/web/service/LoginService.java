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
import edu.pitt.dbmi.ccd.web.domain.LoginCredentials;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.subject.WebSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 28, 2016 11:02:57 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class LoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    private final UserLoginService userLoginService;
    private final EventLogService eventLogService;
    private final UserAccountService userAccountService;
    private final FileManagementService fileManagementService;

    @Autowired
    public LoginService(UserLoginService userLoginService, EventLogService eventLogService, UserAccountService userAccountService, FileManagementService fileManagementService) {
        this.userLoginService = userLoginService;
        this.eventLogService = eventLogService;
        this.userAccountService = userAccountService;
        this.fileManagementService = fileManagementService;
    }

    public Subject passwordLogin(LoginCredentials loginCredentials) {
        String username = loginCredentials.getLoginUsername();
        String password = loginCredentials.getLoginPassword();
        boolean rememberMe = loginCredentials.isRememberMe();

        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(new UsernamePasswordToken(username, password, rememberMe));
        } catch (AuthenticationException exception) {
            LOGGER.info(String.format("Failed login attempt from user %s.", username));
            eventLogService.userLogInFailed(username);
        }

        return currentUser;
    }

    public Subject manualLogin(UserAccount userAccount, HttpServletRequest req, HttpServletResponse res) {
        return new WebSubject.Builder(req, res).authenticated(true).sessionCreationEnabled(true).buildSubject();
    }

    public void logUserInDatabase(UserAccount userAccount) {
        eventLogService.userLogIn(userAccount);
        userLoginService.logUserSignIn(userAccount);

        // reset data after successful login
        if (userAccount.getActivationKey() != null) {
            userAccount.setActivationKey(null);
            userAccountService.save(userAccount);
        }

        // create user directories if not existed
        fileManagementService.createUserDirectories(userAccount);
    }

}
