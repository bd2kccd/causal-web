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
package edu.pitt.dbmi.causal.web.ctrl;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.web.Auth0CallbackHandler;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.UserRegistrationForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.AuthenticationService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserEventLogService;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Aug 17, 2017 1:32:34 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Controller
@SessionAttributes("appUser")
public class Auth0AuthenticationController implements ViewPath {

    private static final String[] UNACTIVATED_ACCOUNT = {"Login Failed!", "Your account has not been activated."};
    private static final String[] LOGIN_FAILED = {"Login Failed!", "Unable to log in at this time."};

    private final AuthenticationService authenticationService;
    private final Auth0CallbackHandler auth0CallbackHandler;
    private final AppUserService appUserService;
    private final UserEventLogService userEventLogService;
    private final UserAccountService userAccountService;

    @Autowired
    public Auth0AuthenticationController(AuthenticationService authenticationService, Auth0CallbackHandler auth0CallbackHandler, AppUserService appUserService, UserEventLogService userEventLogService, UserAccountService userAccountService) {
        this.authenticationService = authenticationService;
        this.auth0CallbackHandler = auth0CallbackHandler;
        this.appUserService = appUserService;
        this.userEventLogService = userEventLogService;
        this.userAccountService = userAccountService;
    }

    @RequestMapping(value = "/portal/home", method = RequestMethod.GET)
    protected String home(
            final Principal principal, final Model model,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        final Auth0User auth0User = (Auth0User) principal;
        final String username = auth0User.getEmail().toLowerCase();
        UserAccount userAccount = userAccountService.getRepository().findByUsername(username);
        if (userAccount == null) {
            redirAttrs.addFlashAttribute("userRegistrationForm", createRegistrationForm(auth0User));

            return REDIRECT_AUTH0_USER_REGISTRATION;
        } else if (userAccount.isActivated()) {
            Subject subject = authenticationService.loginManually(userAccount, req, res);
            if (subject.isAuthenticated()) {
                Date lastLogin = userAccount.getUserLogin().getLoginDate();

                AppUser appUser = createAppUser(auth0User);
                appUser.setLastLogin((lastLogin == null) ? new Date(System.currentTimeMillis()) : lastLogin);
                redirAttrs.addFlashAttribute("appUser", appUser);

                userEventLogService.logUserLogin(userAccount);
                authenticationService.setLoginInfo(userAccount, req.getRemoteAddr());

                return REDIRECT_HOME;
            } else {
                redirAttrs.addFlashAttribute("errorMsg", LOGIN_FAILED);

                return REDIRECT_LOGIN;
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);
            SessionUtils.setTokens(req, null);
            SessionUtils.setAuth0User(req, null);

            return REDIRECT_LOGIN;
        }
    }

    @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        auth0CallbackHandler.handle(req, res);
    }

    public UserRegistrationForm createRegistrationForm(Auth0User auth0User) {
        String firstName = auth0User.getGivenName();
        String lastName = auth0User.getFamilyName();
        String email = auth0User.getEmail().toLowerCase();

        UserRegistrationForm form = new UserRegistrationForm();
        form.setEmail(email);
        form.setFirstName(firstName);
        form.setLastName(lastName);

        return form;
    }

    public AppUser createAppUser(Auth0User auth0User) {
        String firstName = auth0User.getGivenName();
        String lastName = auth0User.getFamilyName();
        String email = auth0User.getEmail().toLowerCase();

        AppUser appUser = appUserService.update(firstName, null, lastName, new AppUser());
        appUser.setUsername(email);
        appUser.setFederatedUser(Boolean.TRUE);

        return appUser;
    }

}
