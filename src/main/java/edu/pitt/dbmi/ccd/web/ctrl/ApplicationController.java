/*
 * Copyright (C) 2015 University of Pittsburgh.
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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * May 14, 2015 12:39:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
public class ApplicationController implements ViewController {

    @Value("${app.webapp:true}")
    private boolean isWebApplication;

    public ApplicationController() {
    }

    @RequestMapping(value = SETUP, method = RequestMethod.GET)
    public String setupNewUser(Model model) {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return REDIRECT_HOME;
        }

        return SETUP;
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String goHome(Model model) {
        return HOME;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logOut(Model model) {
        String url;

        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            model.addAttribute("successMsg", "You Have Successfully Logged Out.");
            url = LOGIN;
        } else {
            url = REDIRECT_LOGIN;
        }

        return url;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(final UsernamePasswordToken credentials, final Model model) {
        String url;

        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(credentials);
            url = REDIRECT_HOME;
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", "Invalid username and/or password.");
            url = LOGIN;
        }

        return url;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage() {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            if (isWebApplication) {
                return LOGIN;
            } else {
                return SETUP;
            }
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage() {
        return showLoginPage();
    }

    @RequestMapping(value = "/404", method = RequestMethod.GET)
    public String showPageNotFound() {
        return "404";
    }

}
