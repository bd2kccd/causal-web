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

import com.auth0.web.Auth0User;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.LoginCredentials;
import edu.pitt.dbmi.ccd.web.domain.user.PasswordRecovery;
import edu.pitt.dbmi.ccd.web.domain.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.Auth0ShiroLoginService;
import edu.pitt.dbmi.ccd.web.service.ShiroLoginService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.subject.WebSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class Auth0ShiroLoginController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0ShiroLoginController.class);

    private static final String LOGIN_VIEW = "auth0ShiroLogin";

    private final Auth0ShiroLoginService auth0ShiroLoginService;

    private final AppUserService appUserService;

    @Autowired
    public Auth0ShiroLoginController(Auth0ShiroLoginService auth0ShiroLoginService, AppUserService appUserService) {
        this.auth0ShiroLoginService = auth0ShiroLoginService;
        this.appUserService = appUserService;
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

        if (!model.containsAttribute("loginCredentials")) {
            model.addAttribute("loginCredentials", new LoginCredentials(true));
        }
        if (!model.containsAttribute("userRegistration")) {
            model.addAttribute("userRegistration", new UserRegistration());
        }
        if (!model.containsAttribute("passwordRecovery")) {
            model.addAttribute("passwordRecovery", new PasswordRecovery());
        }

        auth0ShiroLoginService.setAuth0LockProperties(request, model);

        return LOGIN_VIEW;
    }

    @RequestMapping(value = "${auth0.loginCallback}", method = RequestMethod.GET)
    public String callback(final Model model, final RedirectAttributes redirectAttributes, final HttpServletRequest request, final HttpServletResponse response) {
        Auth0User auth0User = auth0ShiroLoginService.handleCallback(redirectAttributes, request, response);
        if (auth0User == null) {
            return REDIRECT_LOGIN;
        }

        UserAccount userAccount = auth0ShiroLoginService.findUserAccount(auth0User);
        if (userAccount == null) {
            model.addAttribute("appUser", auth0ShiroLoginService.createAppUser(auth0User));

            return TERMS_VIEW;
        } else if (userAccount.isActive()) {
            auth0ShiroLoginService.recordUserLogin(userAccount, model, request);
            model.addAttribute("appUser", appUserService.createAppUser(userAccount, false));

            new WebSubject.Builder(request, response)
                    .authenticated(true)
                    .sessionCreationEnabled(true)
                    .buildSubject();

            return REDIRECT_HOME;
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", ShiroLoginService.UNACTIVATED_ACCOUNT);

            return REDIRECT_LOGIN;
        }
    }

}
