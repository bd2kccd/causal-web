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
package edu.pitt.dbmi.causal.web.service.account;

import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.ChangePasswordForm;
import edu.pitt.dbmi.causal.web.model.account.UserInfoForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserInformation;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
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

    private final AppUserService appUserService;
    private final DefaultPasswordService passwordService;
    private final UserAccountService userAccountService;

    @Autowired
    public UserProfileService(AppUserService appUserService, DefaultPasswordService passwordService, UserAccountService userAccountService) {
        this.appUserService = appUserService;
        this.passwordService = passwordService;
        this.userAccountService = userAccountService;
    }

    public boolean updateUserPassword(ChangePasswordForm changePasswordForm, AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        userAccount.setPassword(passwordService.encryptPassword(changePasswordForm.getNewPassword()));
        try {
            userAccount = userAccountService.getRepository().save(userAccount);
        } catch (Exception exception) {
            LOGGER.error("Unable to update user's password.", exception);
            return false;
        }

        appUserService.updateCache(userAccount);

        return true;
    }

    public boolean passwordMatch(ChangePasswordForm changePasswordForm, AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        String currPwd = changePasswordForm.getCurrentPassword();
        String encryptCurrPwd = userAccount.getPassword();

        return passwordService.passwordsMatch(currPwd, encryptCurrPwd);
    }

    public AppUser updateUserInformation(UserInfoForm userInfoForm, AppUser appUser) {
        UserInformation userInfo = appUserService.retrieveUserInformation(appUser);
        userInfo.setFirstName(userInfoForm.getFirstName());
        userInfo.setMiddleName(userInfoForm.getMiddleName());
        userInfo.setLastName(userInfoForm.getLastName());

        return appUserService.updateUserInformation(userInfo, appUser);
    }

    public UserInfoForm createUserInfoForm(AppUser appUser) {
        UserInformation userInfo = appUserService.retrieveUserInformation(appUser);

        UserInfoForm userInfoForm = new UserInfoForm();
        userInfoForm.setEmail(userInfo.getEmail());
        userInfoForm.setFirstName(userInfo.getFirstName());
        userInfoForm.setMiddleName(userInfo.getMiddleName());
        userInfoForm.setLastName(userInfo.getLastName());

        return userInfoForm;
    }

}
