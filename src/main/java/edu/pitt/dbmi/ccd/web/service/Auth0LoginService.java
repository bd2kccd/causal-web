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

import com.auth0.Auth0User;
import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import com.auth0.web.Auth0CallbackHandler;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserLoginService;
import edu.pitt.dbmi.ccd.web.conf.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.LoginCredentials;
import edu.pitt.dbmi.ccd.web.domain.PasswordRecovery;
import edu.pitt.dbmi.ccd.web.domain.account.UserRegistration;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.subject.WebSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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

    private final Auth0CallbackHandler callback;

    private final CcdProperties ccdProperties;
    private final UserAccountService userAccountService;
    private final UserLoginService userLoginService;
    private final EventLogService eventLogService;
    private final AppUserService appUserService;

    @Autowired
    public Auth0LoginService(Auth0CallbackHandler callback, CcdProperties ccdProperties, UserAccountService userAccountService, UserLoginService userLoginService, EventLogService eventLogService, AppUserService appUserService) {
        this.callback = callback;
        this.ccdProperties = ccdProperties;
        this.userAccountService = userAccountService;
        this.userLoginService = userLoginService;
        this.eventLogService = eventLogService;
        this.appUserService = appUserService;
    }

    public void logInUser(UserAccount userAccount, Auth0User auth0User, HttpServletRequest req, final HttpServletResponse res, Model model) {
        eventLogService.userLogIn(userAccount);
        userLoginService.logUserSignIn(userAccount);

        // reset data after successful login
        userAccount.setActivationKey(null);
        userAccountService.save(userAccount);

        // create user directories if not existed
//        fileManagementService.createUserDirectories(userAccount);
        AppUser appUser = appUserService.createAppUser(userAccount, true);
        appUser.setFirstName(auth0User.getGivenName());
        appUser.setLastName(auth0User.getFamilyName());

        new WebSubject.Builder(req, res)
                .authenticated(true)
                .sessionCreationEnabled(true)
                .buildSubject();

        model.addAttribute("appUser", appUser);
    }

    public AppUser createTempAppUser(Auth0User auth0User) {
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

    public UserAccount retrieveUserAccount(Auth0User auth0User) {
        String username = auth0User.getEmail().toLowerCase().trim();

        return userAccountService.findByEmail(username);
    }

    public void showLoginPage(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("loginCredentials")) {
            model.addAttribute("loginCredentials", new LoginCredentials(true));
        }
        if (!model.containsAttribute("userRegistration")) {
            model.addAttribute("userRegistration", new UserRegistration());
        }
        if (!model.containsAttribute("passwordRecovery")) {
            model.addAttribute("passwordRecovery", new PasswordRecovery());
        }

        detectError(model);
        NonceUtils.addNonceToStorage(request);
        String host = UriTool.buildURI(request, ccdProperties).build().toString();
//        String host = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        model.addAttribute("host", host);
        model.addAttribute("state", SessionUtils.getState(request));
    }

    public void handleCallback(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        callback.handle(req, res);
    }

    private void detectError(Model model) {
        Map<String, Object> modelMap = model.asMap();
        if (modelMap.get("error") == null) {
            modelMap.put("error", Boolean.FALSE);
        } else {
            modelMap.put("error", Boolean.TRUE);
        }
    }

}
