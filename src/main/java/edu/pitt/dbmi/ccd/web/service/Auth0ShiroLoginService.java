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

import com.auth0.Auth0Exception;
import com.auth0.web.Auth0Client;
import com.auth0.web.Auth0Config;
import com.auth0.web.Auth0User;
import com.auth0.web.NonceUtils;
import com.auth0.web.SessionUtils;
import com.auth0.web.Tokens;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 2, 2016 4:50:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Service
public class Auth0ShiroLoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0ShiroLoginService.class);

    private final CcdProperties ccdProperties;

    private final UserAccountService userAccountService;

    private final Auth0Config auth0Config;

    protected final Auth0Client auth0Client;

    @Autowired
    public Auth0ShiroLoginService(CcdProperties ccdProperties, UserAccountService userAccountService, Auth0Config auth0Config, Auth0Client auth0Client) {
        this.ccdProperties = ccdProperties;
        this.userAccountService = userAccountService;
        this.auth0Config = auth0Config;
        this.auth0Client = auth0Client;
    }

    public void recordUserLogin(UserAccount userAccount, Model model, HttpServletRequest request) {
        UserLogin userLogin = userAccount.getUserLogin();
        userLogin.setLastLoginDate(userLogin.getLoginDate());
        userLogin.setLastLoginLocation(userLogin.getLoginLocation());
        userLogin.setLoginDate(new Date(System.currentTimeMillis()));
        try {
            userLogin.setLoginLocation(UriTool.InetNTOA(request.getRemoteAddr()));
        } catch (UnknownHostException exception) {
            LOGGER.info(exception.getLocalizedMessage());
        }
        userAccountService.saveUserAccount(userAccount);
    }

    public AppUser createAppUser(Auth0User auth0User) {
        String firstName = auth0User.getGivenName();
        String lastName = auth0User.getFamilyName();
        String email = auth0User.getEmail().toLowerCase();

        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setFirstName((firstName == null) ? "" : firstName);
        appUser.setMiddleName("");
        appUser.setLastName((lastName == null) ? "" : lastName);
        appUser.setFederatedUser(Boolean.TRUE);

        return appUser;
    }

    public UserAccount findUserAccount(Auth0User auth0User) {
        return userAccountService.findByEmail(auth0User.getEmail().toLowerCase());
    }

    public void setAuth0LockProperties(HttpServletRequest request, Model model) {
        NonceUtils.addNonceToStorage(request);

        String host = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        model.addAttribute("host", host);
        model.addAttribute("state", SessionUtils.getState(request));
    }

    public Auth0User handleCallback(RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) throws Auth0Exception {
        Auth0User auth0User = null;
        try {
            if (isValidRequest(request)) {
                Tokens tokens = fetchTokens(request);
                auth0User = auth0Client.getUserProfile(tokens);
                NonceUtils.removeNonceFromStorage(request);
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Invalid state or error");
            }
        } catch (IOException exception) {
            LOGGER.error("Auth0 login failed.", exception);
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid state or error");
        }

        return auth0User;
    }

    protected Tokens fetchTokens(final HttpServletRequest request) {
        final String authorizationCode = request.getParameter("code");
        final String redirectUri = request.getRequestURL().toString();
        return auth0Client.getTokens(authorizationCode, redirectUri);
    }

    protected boolean isValidRequest(final HttpServletRequest request) throws IOException {
        return !hasError(request) && isValidState(request);
    }

    protected boolean hasError(final HttpServletRequest req) {
        return req.getParameter("error") != null;
    }

    protected boolean isValidState(final HttpServletRequest request) {
        final String stateFromRequest = request.getParameter("state");
        return NonceUtils.matchesNonceInStorage(request, stateFromRequest);
    }

}
