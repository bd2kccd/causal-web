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

import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.LoginForm;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.cache.annotation.CacheEvict;
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

    private static final String LOGOUT_SUCCESS = "You have successfully logged out.";

    private static final String LOGIN_VIEW = "auth0-shiro-login";

    public Auth0LoginController() {
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

        initLoginPage(model, req);

        return LOGIN_VIEW;
    }

    @CacheEvict(cacheNames = {"appUserServiceUserAccount"}, key = "#appUser.username")
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

            if (appUser.isFederatedUser()) {
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
