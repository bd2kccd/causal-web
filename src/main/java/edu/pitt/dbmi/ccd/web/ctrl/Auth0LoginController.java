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
package edu.pitt.dbmi.ccd.web.ctrl;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.service.ApplicationService;
import edu.pitt.dbmi.ccd.web.service.Auth0LoginService;
import java.io.IOException;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    private static final String LOGIN_VIEW = "auth0ShiroLogin";

    private final Auth0LoginService auth0LoginService;

    @Autowired
    public Auth0LoginController(Auth0LoginService auth0LoginService) {
        this.auth0LoginService = auth0LoginService;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(
            final SessionStatus sessionStatus,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
        } else if (currentUser.isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            sessionStatus.setComplete();
        }

        auth0LoginService.showLoginPage(model, request);

        return LOGIN_VIEW;
    }

    @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        auth0LoginService.handleCallback(req, res);
    }

    @RequestMapping(value = "/portal/home", method = RequestMethod.GET)
    protected String home(final Principal principal, final Model model, final RedirectAttributes redirectAttributes, final HttpServletRequest req, final HttpServletResponse res) {
        final Auth0User auth0User = (Auth0User) principal;

        UserAccount userAccount = auth0LoginService.retrieveUserAccount(auth0User);
        if (userAccount == null) {
            model.addAttribute("appUser", auth0LoginService.createTempAppUser(auth0User));

            return TERMS_VIEW;
        } else if (userAccount.isActivated()) {
            auth0LoginService.logInUser(userAccount, auth0User, redirectAttributes, req, res, model);

            return REDIRECT_HOME;
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", ApplicationService.UNACTIVATED_ACCOUNT);
            SessionUtils.setTokens(req, null);
            SessionUtils.setAuth0User(req, null);
            
            return REDIRECT_LOGIN;
        }
    }

}
