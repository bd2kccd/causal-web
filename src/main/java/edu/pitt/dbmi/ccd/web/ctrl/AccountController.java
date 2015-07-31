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

import edu.pitt.dbmi.ccd.db.entity.SecurityAnswer;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.SecurityAnswerService;
import edu.pitt.dbmi.ccd.db.service.SecurityQuestionService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.ResetPasswordInfo;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * Jul 30, 2015 2:07:05 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "account")
public class AccountController implements ViewController {

    private final UserAccountService userAccountService;

    private final SecurityAnswerService securityAnswerService;

    private final SecurityQuestionService securityQuestionService;

    @Autowired(required = true)
    public AccountController(
            UserAccountService userAccountService,
            SecurityAnswerService securityAnswerService,
            SecurityQuestionService securityQuestionService) {
        this.userAccountService = userAccountService;
        this.securityAnswerService = securityAnswerService;
        this.securityQuestionService = securityQuestionService;
    }

    @RequestMapping(value = "reset/pwd", method = RequestMethod.POST)
    public String processUsernameRequest(
            @RequestParam(value = "type") String type,
            @ModelAttribute("info") final ResetPasswordInfo info,
            final Model model) {
        String username = info.getUsername();
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            model.addAttribute("strErrMsg", "Invalid Username!");
            model.addAttribute("errMsg", "Sorry, we have no record of that username.");
            model.addAttribute("info", info);

            return "account/reqestUsername";
        }

        List<SecurityAnswer> list = securityAnswerService.findByUserAccounts(Collections.singleton(userAccount));
        if (list.isEmpty()) {
            model.addAttribute("strErrMsg", "Security Question Not Set!");
            model.addAttribute("errMsg", "Please contact the administrator to reset your password.");
            model.addAttribute("info", info);

            return "account/reqestUsername";
        }

        switch (type) {
            case "usr":
                list.forEach(item -> {
                    info.setQuestion(item.getSecurityQuestion().getQuestion());
                });
                model.addAttribute("info", info);
                return "account/reqestAnswer";
            case "ans":
                String ans = "";
                for (SecurityAnswer item : list) {
                    ans = item.getAnswer();
                }
                return "s";
            default:
                return "account/reqestAnswer";
        }
    }

    @RequestMapping(value = "reset/pwd", method = RequestMethod.GET)
    public String showUsernameRequest(final Model model) {
        model.addAttribute("info", new ResetPasswordInfo("", "", "", ""));

        return "account/reqestUsername";
    }

}
