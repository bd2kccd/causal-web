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

import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.LoginForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 4, 2016 12:16:46 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("shiro")
@Controller
@SessionAttributes("appUser")
public class ShiroLoginController {

    private static final String LOGOUT_SUCCESS = "You Have Successfully Logged Out.";

    @GetMapping(SitePaths.LOGIN)
    public String showLoginPage(final SessionStatus sessionStatus, final Model model, HttpServletRequest req) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
        } else if (currentUser.isAuthenticated()) {
            return SitePaths.REDIRECT_HOME;
        } else {
            sessionStatus.setComplete();
        }

        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm(true));
        }

        return SiteViews.SHIRO_LOGIN;
    }

    @CacheEvict(cacheNames = {"appUserServiceUserAccount"}, key = "#appUser.username")
    @GetMapping(SitePaths.LOGOUT)
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
        }

        HttpSession httpSession = req.getSession();
        if (httpSession != null) {
            httpSession.invalidate();
        }

        return SitePaths.REDIRECT_LOGIN;
    }

}
