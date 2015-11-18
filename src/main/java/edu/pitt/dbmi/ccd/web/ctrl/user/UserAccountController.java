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
package edu.pitt.dbmi.ccd.web.ctrl.user;

import edu.pitt.dbmi.ccd.db.entity.SecurityAnswer;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.SecurityAnswerService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.ResetPasswordInfo;
import java.util.Collections;
import java.util.List;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * Aug 5, 2015 10:04:54 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "/user/account")
public class UserAccountController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountController.class);

    private final UserAccountService userAccountService;

    private final SecurityAnswerService securityAnswerService;

    private final DefaultPasswordService passwordService;

    @Autowired(required = true)
    public UserAccountController(
            UserAccountService userAccountService,
            SecurityAnswerService securityAnswerService,
            DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.securityAnswerService = securityAnswerService;
        this.passwordService = passwordService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @RequestMapping(value = "reset/pwd", method = RequestMethod.POST)
    public String processUsernameRequest(@ModelAttribute("info") final ResetPasswordInfo info, final Model model) {
        String username = info.getUsername();
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            model.addAttribute("strErrMsg", "Invalid Username!");
            model.addAttribute("errMsg", "Sorry, we have no record of that username.");
            model.addAttribute("info", info);

            return REQUEST_USERNAME_VIEW;
        }

        List<SecurityAnswer> list = securityAnswerService.findByUserAccounts(Collections.singleton(userAccount));
        if (list.isEmpty()) {
            model.addAttribute("strErrMsg", "Security Question Not Set!");
            model.addAttribute("errMsg", "Please contact the administrator to reset your password.");
            model.addAttribute("info", info);

            return REQUEST_USERNAME_VIEW;
        }

        list.forEach(item -> {
            info.setQuestion(item.getSecurityQuestion().getQuestion());
        });

        String answer = info.getAnswer();
        if (answer == null || answer.trim().length() == 0) {
            model.addAttribute("info", info);
            return REQUEST_ANSWER_VIEW;
        }

        String ans = "";
        for (SecurityAnswer item : list) {
            ans = item.getAnswer();
        }
        if (!ans.equalsIgnoreCase(answer)) {
            model.addAttribute("strErrMsg", "Incorrect Security Answer!");
            model.addAttribute("errMsg", "Please try again.");
            info.setAnswer("");
            model.addAttribute("info", info);
            return REQUEST_ANSWER_VIEW;
        }

        String password = info.getPassword();
        if (password == null || password.trim().length() == 0) {
            model.addAttribute("info", info);
            return REQUEST_NEW_PASSWORD_VIEW;
        }

        userAccount.setPassword(passwordService.encryptPassword(password));
        try {
            userAccountService.saveUserAccount(userAccount);
        } catch (Exception exception) {
            LOGGER.warn(
                    String.format("Unable to reset password for %s.", userAccount.getUsername()),
                    exception);
            info.setAnswer("");
            info.setPassword("");
            info.setQuestion("");
            info.setUsername("");
            model.addAttribute("errMsg", "error");
            return REQUEST_PASSWORD_CHANGED_DONE_VIEW;
        }

        return REQUEST_PASSWORD_CHANGED_DONE_VIEW;
    }

    @RequestMapping(value = "reset/pwd", method = RequestMethod.GET)
    public String showUsernameRequest(final Model model) {
        model.addAttribute("info", new ResetPasswordInfo("", "", "", ""));

        return REQUEST_USERNAME_VIEW;
    }

}
