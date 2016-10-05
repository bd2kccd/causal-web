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
package edu.pitt.dbmi.ccd.web.service.account;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.user.PasswordChange;
import edu.pitt.dbmi.ccd.web.model.user.UserInfo;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 5, 2016 4:06:29 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    private static final String[] USER_PROFILE_UPDATE_FAILED = {"User Profile Update Failed!", "Unable to update user information."};
    private static final String[] PASSWORD_UPDATE_FAILED = {"Password Update Failed!", "Unable to change password."};

    private final UserAccountService userAccountService;
    private final AppUserService appUserService;
    private final DefaultPasswordService passwordService;

    @Autowired
    public UserProfileService(UserAccountService userAccountService, AppUserService appUserService, DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
        this.passwordService = passwordService;
    }

    public UserInfo createUserInfo(AppUser appUser) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        Person person = userAccount.getPerson();

        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(person.getEmail());
        userInfo.setFirstName(person.getFirstName());
        userInfo.setMiddleName(person.getMiddleName());
        userInfo.setLastName(person.getLastName());
        userInfo.setUsername(userAccount.getUsername());

        return userInfo;
    }

    public void updateUserProfile(UserInfo userInfo, AppUser appUser, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String username = appUser.getUsername();
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount != null) {
            try {
                Person person = userAccount.getPerson();
                person.setFirstName(userInfo.getFirstName());
                person.setMiddleName(userInfo.getMiddleName());
                person.setLastName(userInfo.getLastName());
                person = userAccountService.updatePerson(userAccount);

                model.addAttribute("appUser", appUserService.updateUserProfile(appUser, person));
            } catch (Exception exception) {
                LOGGER.error(String.format("Unable to update user '%s' information.", username), exception);
                redirectAttributes.addFlashAttribute("profileChangeError", USER_PROFILE_UPDATE_FAILED);
            }
        }
    }

    public void updateUserPassword(PasswordChange passwordChange, AppUser appUser, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String username = appUser.getUsername();
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount != null) {
            String currentPwd = passwordChange.getCurrentPassword();
            String encryptedPwd = userAccount.getPassword();
            if (passwordService.passwordsMatch(currentPwd, encryptedPwd)) {
                userAccount.setPassword(passwordService.encryptPassword(passwordChange.getNewPassword()));
                try {
                    userAccountService.updateAccount(userAccount);
                } catch (Exception exception) {
                    LOGGER.error(String.format("Unable to update user '%s' password.", username), exception);
                    redirectAttributes.addFlashAttribute("pwdChangeErrMsg", PASSWORD_UPDATE_FAILED);
                    redirectAttributes.addFlashAttribute("pwdChangeErr", true);
                }
            } else {
                redirectAttributes.addFlashAttribute("errInvalidPwd", "Invalid password.");
                redirectAttributes.addFlashAttribute("pwdChangeErr", true);
            }
        }
    }

}
