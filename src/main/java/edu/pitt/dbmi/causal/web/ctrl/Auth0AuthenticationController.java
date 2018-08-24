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
package edu.pitt.dbmi.causal.web.ctrl;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.web.Auth0CallbackHandler;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.UserRegistrationForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.AuthService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.io.IOException;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
public class Auth0AuthenticationController {

    private static final String[] UNACTIVATED_ACCOUNT = {"Login Failed!", "Your account has not been activated."};
    private static final String[] LOGIN_FAILED = {"Login Failed!", "Unable to log in at this time."};

    private final Auth0CallbackHandler auth0CallbackHandler;
    private final UserAccountService userAccountService;
    private final AppUserService appUserService;
    private final AuthService authService;

    @Autowired
    public Auth0AuthenticationController(Auth0CallbackHandler auth0CallbackHandler, UserAccountService userAccountService, AppUserService appUserService, AuthService authService) {
        this.auth0CallbackHandler = auth0CallbackHandler;
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
        this.authService = authService;
    }

    @GetMapping("/portal/home")
    protected String home(
            final Principal principal,
            final Model model,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        final Auth0User auth0User = (Auth0User) principal;
        final String username = auth0User.getEmail().toLowerCase();

        UserAccount userAccount = userAccountService.getRepository()
                .findByUsername(username);
        if (userAccount == null) {
            redirAttrs.addFlashAttribute("userRegistrationForm", createRegistrationForm(auth0User));

            return SitePaths.REDIRECT_AUTH0_USER_REGISTRATION;
        } else if (userAccount.isActivated()) {
            Subject subject = authService.login(userAccount, req, res);
            if (subject.isAuthenticated()) {
                String firstName = auth0User.getGivenName();
                String lastName = auth0User.getFamilyName();

                AppUser appUser = appUserService.create(userAccount, true, req.getRemoteAddr());
                appUser.setFirstName(firstName == null ? "" : firstName);
                appUser.setMiddleName("");
                appUser.setLastName(lastName == null ? "" : lastName);

                redirAttrs.addFlashAttribute("appUser", appUser);

                return SitePaths.REDIRECT_HOME;
            } else {
                redirAttrs.addFlashAttribute("errorMsg", LOGIN_FAILED);

                return SitePaths.REDIRECT_LOGIN;
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);
            SessionUtils.setTokens(req, null);
            SessionUtils.setAuth0User(req, null);

            return SitePaths.REDIRECT_LOGIN;
        }
    }

    @GetMapping("${auth0.loginCallback}")
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

}
