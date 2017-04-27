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

import com.auth0.Auth0User;
import com.auth0.web.Auth0CallbackHandler;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 2, 2016 4:50:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Service
public class Auth0LoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0LoginService.class);

    private final UserAccountService userAccountService;
    private final Auth0CallbackHandler auth0CallbackHandler;

    @Autowired
    public Auth0LoginService(UserAccountService userAccountService, Auth0CallbackHandler auth0CallbackHandler) {
        this.userAccountService = userAccountService;
        this.auth0CallbackHandler = auth0CallbackHandler;
    }

    public void handleCallback(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        auth0CallbackHandler.handle(req, res);
    }

    public void logInUser(UserAccount userAccount) {
    }

    public UserAccount retrieveUserAccount(Auth0User auth0User) {
        UserAccount userAccount = null;

        String email = auth0User.getEmail().toLowerCase().trim();
        try {
            userAccount = userAccountService.findByEmail(email);
        } catch (Exception exception) {
            LOGGER.error("Unable to retrieve user by email.", exception);
        }

        return userAccount;
    }

}
