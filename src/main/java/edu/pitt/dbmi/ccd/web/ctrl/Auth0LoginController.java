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
package edu.pitt.dbmi.ccd.web.ctrl;

import com.auth0.Auth0User;
import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.LoginForm;
import edu.pitt.dbmi.ccd.web.domain.account.PasswordResetRequestForm;
import edu.pitt.dbmi.ccd.web.domain.account.UserRegistrationForm;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.Auth0LoginService;
import edu.pitt.dbmi.ccd.web.service.AuthenticationService;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 2, 2016 4:45:18 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Controller
@SessionAttributes("appUser")
public class Auth0LoginController implements ViewPath {

    private static final String[] LOGOUT_SUCCESS = {"You Have Successfully Logged Out."};
    private static final String[] UNACTIVATED_ACCOUNT = {"Login Failed!", "Your account has not been activated."};
    private static final String[] LOGIN_FAILED = {"Login Failed!", "Unable to log in at this time."};

    private static final String LOGIN_VIEW = "auth0ShiroLogin";

    private final Auth0LoginService auth0LoginService;
    private final AuthenticationService authenticationService;
    private final AppUserService appUserService;

    @Autowired
    public Auth0LoginController(
            Auth0LoginService auth0LoginService,
            AuthenticationService authenticationService,
            AppUserService appUserService) {
        this.auth0LoginService = auth0LoginService;
        this.authenticationService = authenticationService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(final SessionStatus sessionStatus, final Model model, HttpServletRequest req) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
        } else if (currentUser.isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            sessionStatus.setComplete();
        }

        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm(true));
        }
        if (!model.containsAttribute("userRegistrationForm")) {
            model.addAttribute("userRegistrationForm", new UserRegistrationForm());
        }
        if (!model.containsAttribute("passwordResetRequestForm")) {
            model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        }

        initLoginPage(model, req);

        return LOGIN_VIEW;
    }

    @RequestMapping(value = LOGOUT, method = RequestMethod.GET)
    public String logOut(
            @ModelAttribute("appUser") final AppUser appUser,
            final SessionStatus sessionStatus,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();

            redirAttrs.addFlashAttribute("successMsg", LOGOUT_SUCCESS);

            if (appUser.getFederatedUser()) {
                SessionUtils.setTokens(req, null);
                SessionUtils.setAuth0User(req, null);
            }
        }

        HttpSession httpSession = req.getSession();
        if (httpSession != null) {
            httpSession.invalidate();
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        auth0LoginService.handleCallback(req, res);
    }

    @RequestMapping(value = "/portal/home", method = RequestMethod.GET)
    protected String home(
            final Principal principal, final Model model,
            final RedirectAttributes redirAttrs,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        final Auth0User auth0User = (Auth0User) principal;
        UserAccount userAccount = auth0LoginService.retrieveUserAccount(auth0User);
        if (userAccount == null) {
            model.addAttribute("appUser", appUserService.create(auth0User));

            return TERMS_VIEW;
        } else if (userAccount.isActivated()) {
            Subject subject = authenticationService.loginManually(userAccount, req, res);
            if (subject.isAuthenticated()) {
                Date lastLogin = userAccount.getUserLogin().getLoginDate();

                AppUser appUser = appUserService.create(auth0User);
                appUser.setLastLogin((lastLogin == null) ? new Date(System.currentTimeMillis()) : lastLogin);
                redirAttrs.addFlashAttribute("appUser", appUser);

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

    private void initLoginPage(Model model, HttpServletRequest req) {
        // detect error
        Map<String, Object> modelMap = model.asMap();
        if (modelMap.get("error") == null) {
            modelMap.put("error", Boolean.FALSE);
        } else {
            modelMap.put("error", Boolean.TRUE);
        }

        NonceUtils.addNonceToStorage(req);
        model.addAttribute("state", SessionUtils.getState(req));
    }

}
