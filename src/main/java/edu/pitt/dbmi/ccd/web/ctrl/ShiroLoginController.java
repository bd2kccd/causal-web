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

import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.LoginForm;
import edu.pitt.dbmi.ccd.web.model.account.PasswordResetRequestForm;
import edu.pitt.dbmi.ccd.web.model.account.UserRegistrationForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
 * Feb 18, 2016 1:37:14 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("shiro")
@Controller
@SessionAttributes("appUser")
public class ShiroLoginController implements ViewPath {

    private static final String[] LOGOUT_SUCCESS = {"You Have Successfully Logged Out."};

    private static final String LOGIN_VIEW = "shiroLogin";

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
        }

        HttpSession httpSession = req.getSession();
        if (httpSession != null) {
            httpSession.invalidate();
        }

        return REDIRECT_LOGIN;
    }

}
