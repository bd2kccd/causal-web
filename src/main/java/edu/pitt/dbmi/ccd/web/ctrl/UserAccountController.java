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

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.service.UserAccountService;
import java.util.Date;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * May 14, 2015 2:13:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
public class UserAccountController implements ViewController {

    private final UserAccountService userAccountService;

    private final DefaultPasswordService passwordService;

    @Autowired(required = true)
    public UserAccountController(UserAccountService userAccountService, DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.passwordService = passwordService;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerNewUser(final UserAccount userAccount, Model model) {
        String username = userAccount.getUsername();
        if (userAccountService.findByUsername(username) != null) {
            model.addAttribute("errorMsg", String.format("Username '%s' is already taken.", username));
            return LOGIN;
        }

        String pwd = userAccount.getPassword();
        userAccount.setPassword(passwordService.encryptPassword(pwd));
        userAccount.setActive(true);
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setPerson(new Person("Default", "User", "user@localhost"));
        try {
            userAccountService.createNewUserAccount(userAccount);
        } catch (Exception exception) {
            model.addAttribute("errorMsg", String.format("Unable to create account for '%s'.", username));
            return LOGIN;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), pwd);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", "Unable to sign in.");
            return LOGIN;
        }

        return REDIRECT_HOME;
    }

}
