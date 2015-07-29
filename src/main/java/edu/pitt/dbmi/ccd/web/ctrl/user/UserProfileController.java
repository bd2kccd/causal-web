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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.user.PasswordChange;
import edu.pitt.dbmi.ccd.web.model.user.UserInfo;
import edu.pitt.dbmi.ccd.web.model.user.UserWorkspace;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
public class UserProfileController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileController.class);

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;

    private final DefaultPasswordService passwordService;

    @Autowired(required = true)
    public UserProfileController(
            UserAccountService userAccountService,
            AppUserService appUserService,
            DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
        this.passwordService = passwordService;
    }

    @RequestMapping(value = "pwd", method = RequestMethod.POST)
    public String updateUserPassword(
            @ModelAttribute("userWorkspace") final PasswordChange passwordChange,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        String currentPwd = passwordChange.getCurrentPassword();
        String encryptedPwd = userAccount.getPassword();

        if (passwordService.passwordsMatch(currentPwd, encryptedPwd)) {
            String newPwd = passwordChange.getNewPassword();
            userAccount.setPassword(passwordService.encryptPassword(newPwd));
            userAccountService.save(userAccount);
        } else {
            redirectAttributes.addFlashAttribute("pwdChangeErr", "Invalid password.");
        }

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(value = "workspace", method = RequestMethod.POST)
    public String updateUserWorkspace(
            @ModelAttribute("userWorkspace") final UserWorkspace userWorkspace,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {

        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());

        // update person information
        Person person = userAccount.getPerson();
        person.setWorkspace(userWorkspace.getWorkspace());

        userAccount = userAccountService.save(userAccount);

        model.addAttribute("appUser", appUserService.createAppUser(userAccount));

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(value = "info", method = RequestMethod.POST)
    public String updateUserInfo(
            @ModelAttribute("userInfo") final UserInfo userInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());

        // update person information
        Person person = userAccount.getPerson();
        person.setEmail(userInfo.getEmail());
        person.setFirstName(userInfo.getFirstName());
        person.setLastName(userInfo.getLastName());

        userAccount = userAccountService.save(userAccount);

        model.addAttribute("appUser", appUserService.createAppUser(userAccount));

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showPageUserProfile(
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        Person person = userAccount.getPerson();

        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(person.getEmail());
        userInfo.setFirstName(person.getFirstName());
        userInfo.setLastName(person.getLastName());

        UserWorkspace userWorkspace = new UserWorkspace();
        userWorkspace.setWorkspace(person.getWorkspace());

        PasswordChange passwordChange = new PasswordChange();
        passwordChange.setCurrentPassword("");
        passwordChange.setNewPassword("");
        passwordChange.setConfirmPassword("");

        model.addAttribute("userInfo", userInfo);
        model.addAttribute("userWorkspace", userWorkspace);
        model.addAttribute("passwordChange", passwordChange);

        return USER_PROFILE;
    }

}
