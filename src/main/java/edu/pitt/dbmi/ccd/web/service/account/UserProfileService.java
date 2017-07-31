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
package edu.pitt.dbmi.ccd.web.service.account;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserInfo;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserInfoService;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.account.PasswordChangeForm;
import edu.pitt.dbmi.ccd.web.model.account.UserInfoForm;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 5, 2016 4:06:29 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    private final UserAccountService userAccountService;
    private final UserInfoService userInfoService;

    private final DefaultPasswordService passwordService;

    @Autowired
    public UserProfileService(UserAccountService userAccountService, UserInfoService userInfoService, DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.userInfoService = userInfoService;
        this.passwordService = passwordService;
    }

    public boolean passwordMatch(PasswordChangeForm passwordChangeForm, UserAccount userAccount) {
        String currPwd = passwordChangeForm.getCurrentPassword();
        String encryptCurrPwd = userAccount.getPassword();

        return passwordService.passwordsMatch(currPwd, encryptCurrPwd);
    }

    public boolean updateUserAccountPassword(PasswordChangeForm passwordChangeForm, UserAccount userAccount) {
        String newPwd = passwordChangeForm.getNewPassword();
        userAccount.setPassword(passwordService.encryptPassword(newPwd));
        try {
            userAccountService.getRepository().save(userAccount);
        } catch (Exception exception) {
            LOGGER.error("Unable to update user's password.", exception);
            return false;
        }

        return true;
    }

    public boolean updateUserInfo(UserInfoForm userInfoForm, UserAccount userAccount) {
        String firstName = userInfoForm.getFirstName();
        String middleName = userInfoForm.getMiddleName();
        String lastName = userInfoForm.getLastName();

        UserInfo userInfo = userAccount.getUserInfo();
        userInfo.setFirstName(firstName);
        userInfo.setMiddleName(middleName);
        userInfo.setLastName(lastName);

        try {
            userInfoService.getRepository().save(userInfo);
        } catch (Exception exception) {
            LOGGER.error("Unable to update user information.", exception);
            return false;
        }

        return true;
    }

    public UserInfoForm populateUserInfoForm(AppUser appUser) {
        UserInfoForm userInfoForm = new UserInfoForm();

        UserAccount userAccount = userAccountService.getRepository().findByUsername(appUser.getUsername());
        if (userAccount != null) {
            UserInfo userInfo = userAccount.getUserInfo();
            userInfoForm.setEmail(userInfo.getEmail());
            userInfoForm.setFirstName(userInfo.getFirstName());
            userInfoForm.setMiddleName(userInfo.getMiddleName());
            userInfoForm.setLastName(userInfo.getLastName());
        }

        return userInfoForm;
    }

}
