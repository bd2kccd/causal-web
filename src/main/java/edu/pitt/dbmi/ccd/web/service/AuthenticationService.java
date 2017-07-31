/*
 * Copyright (C) 2017 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.commons.uri.InetUtils;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserLoginService;
import edu.pitt.dbmi.ccd.web.model.LoginForm;
import java.util.Date;
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
 * Oct 4, 2016 3:27:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserAccountService userAccountService;
    private final UserLoginService userLoginService;

    @Autowired
    public AuthenticationService(UserAccountService userAccountService, UserLoginService userLoginService) {
        this.userAccountService = userAccountService;
        this.userLoginService = userLoginService;
    }

    public Subject loginWithUsernamePassword(LoginForm loginForm) {
        String username = loginForm.getEmail();
        String password = loginForm.getPassword();
        boolean rememberMe = loginForm.isRememberMe();

        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(new UsernamePasswordToken(username, password, rememberMe));
        } catch (AuthenticationException exception) {
            LOGGER.info(String.format("Failed login attempt from user %s.", username));
        }

        return currentUser;
    }

    public Subject loginManually(UserAccount userAccount, HttpServletRequest req, HttpServletResponse res) {
        return new WebSubject.Builder(req, res).authenticated(true).sessionCreationEnabled(true).buildSubject();
    }

    public UserAccount retrieveUserAccount(Subject subject) {
        String username = (String) subject.getPrincipals().getPrimaryPrincipal();

        return userAccountService.getRepository().findByUsername(username);
    }

    public void setLoginInfo(UserAccount userAccount, String ipAddress) {
        UserLogin userLogin = userAccount.getUserLogin();
        userLogin.setLoginDate(new Date(System.currentTimeMillis()));
        userLogin.setLoginLocation(InetUtils.getInetNTOA(ipAddress));

        userLoginService.getRepository().save(userLogin);
    }

}
