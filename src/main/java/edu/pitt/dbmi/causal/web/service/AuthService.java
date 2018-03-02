/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.service;

import edu.pitt.dbmi.causal.web.model.LoginForm;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
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
 * Authentication service.
 *
 * Oct 4, 2016 3:27:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountService userAccountService;

    @Autowired
    public AuthService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * Log in using username and password.
     *
     * @param loginForm
     * @return
     */
    public Subject login(LoginForm loginForm) {
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

    /**
     * Manually log in.
     *
     * @param userAccount
     * @param req
     * @param res
     * @return
     */
    public Subject login(UserAccount userAccount, HttpServletRequest req, HttpServletResponse res) {
        return new WebSubject.Builder(req, res).authenticated(true).sessionCreationEnabled(true).buildSubject();
    }

    public UserAccount retrieveAccount(Subject subject) {
        String username = (String) subject.getPrincipals().getPrimaryPrincipal();

        return userAccountService.getRepository().findByUsername(username);
    }

}
