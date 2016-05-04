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

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.SecurityAnswer;
import edu.pitt.dbmi.ccd.db.entity.SecurityQuestion;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.SecurityAnswerService;
import edu.pitt.dbmi.ccd.db.service.SecurityQuestionService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_USER_PROFILE;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.user.PasswordChange;
import edu.pitt.dbmi.ccd.web.model.user.UserInfo;
import edu.pitt.dbmi.ccd.web.model.user.UserSecurityQuestionAnswer;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import java.util.Collections;
import java.util.List;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jul 28, 2015 10:50:40 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "user/profile")
public class UserProfileController implements ViewPath {

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;

    private final DefaultPasswordService passwordService;

    private final SecurityAnswerService securityAnswerService;

    private final SecurityQuestionService securityQuestionService;

    @Autowired
    public UserProfileController(
            UserAccountService userAccountService,
            AppUserService appUserService,
            DefaultPasswordService passwordService,
            SecurityAnswerService securityAnswerService,
            SecurityQuestionService securityQuestionService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
        this.passwordService = passwordService;
        this.securityAnswerService = securityAnswerService;
        this.securityQuestionService = securityQuestionService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    @RequestMapping(value = "security", method = RequestMethod.POST)
    public String updateSecurityQuestionAnswer(
            @ModelAttribute("usqa") final UserSecurityQuestionAnswer usqa,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername()).get();

        List<SecurityAnswer> list = securityAnswerService.findByUserAccounts(Collections.singleton(userAccount));
        if (list.isEmpty()) {
            SecurityAnswer securityAnswer = new SecurityAnswer();
            securityAnswer.setAnswer(usqa.getAnswer());
            securityAnswer.setSecurityQuestion(usqa.getSecurityQuestion());
            securityAnswer.setUserAccounts(Collections.singleton(userAccount));
            list.add(securityAnswer);
        } else {
            list.forEach(securityAnswer -> {
                securityAnswer.setAnswer(usqa.getAnswer());
                securityAnswer.setSecurityQuestion(usqa.getSecurityQuestion());
            });
        }

        securityAnswerService.saveSecurityAnswer(list);

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(value = "pwd", method = RequestMethod.POST)
    public String updateUserPassword(
            @ModelAttribute("userWorkspace") final PasswordChange passwordChange,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername()).get();
        String currentPwd = passwordChange.getCurrentPassword();
        String encryptedPwd = userAccount.getPassword();

        if (passwordService.passwordsMatch(currentPwd, encryptedPwd)) {
            String newPwd = passwordChange.getNewPassword();
            userAccount.setPassword(passwordService.encryptPassword(newPwd));
            userAccountService.saveUserAccount(userAccount);
        } else {
            redirectAttributes.addFlashAttribute("pwdChangeErr", "Invalid password.");
        }

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(value = "info", method = RequestMethod.POST)
    public String updateUserInfo(
            @ModelAttribute("userInfo") final UserInfo userInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername()).get();

        // update person information
        Person person = userAccount.getPerson();
//        person.setEmail(userInfo.getEmail());
        person.setFirstName(userInfo.getFirstName());
        person.setMiddleName(userInfo.getMiddleName());
        person.setLastName(userInfo.getLastName());

        userAccount = userAccountService.saveUserAccount(userAccount);

        AppUser user = appUserService.createAppUser(userAccount);
        user.setLastLogin(appUser.getLastLogin());
        model.addAttribute("appUser", user);

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showUserProfilePage(
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername()).get();
        Person person = userAccount.getPerson();

        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(person.getEmail());
        userInfo.setFirstName(person.getFirstName());
        userInfo.setMiddleName(person.getMiddleName());
        userInfo.setLastName(person.getLastName());
        model.addAttribute("userInfo", userInfo);

        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setCurrentPassword("");
        passwordChange.setNewPassword("");
        passwordChange.setConfirmPassword("");
        model.addAttribute("passwordChange", passwordChange);

        UserSecurityQuestionAnswer usqa = null;
        List<SecurityAnswer> securityAnswers = securityAnswerService
                .findByUserAccounts(Collections.singleton(userAccount));
        for (SecurityAnswer sa : securityAnswers) {
            usqa = new UserSecurityQuestionAnswer(sa.getSecurityQuestion(), sa.getAnswer());
        }
        if (usqa == null) {
            usqa = new UserSecurityQuestionAnswer();
            usqa.setSecurityQuestion(new SecurityQuestion());
        }
        model.addAttribute("usqa", usqa);
        model.addAttribute("securityQuestions", securityQuestionService.findAllSecurityQuestion());

        return USER_PROFILE_VIEW;
    }

}
