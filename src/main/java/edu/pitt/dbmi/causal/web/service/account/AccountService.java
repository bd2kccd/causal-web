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
import edu.pitt.dbmi.causal.web.model.account.PasswordUpdateForm;
import edu.pitt.dbmi.causal.web.model.account.UserProfileUpdateForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserProfile;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 6, 2018 2:19:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    private final AppUserService appUserService;
    private final DefaultPasswordService passwordService;
    private final UserAccountService userAccountService;

    @Autowired
    public AccountService(AppUserService appUserService, DefaultPasswordService passwordService, UserAccountService userAccountService) {
        this.appUserService = appUserService;
        this.passwordService = passwordService;
        this.userAccountService = userAccountService;
    }

    public boolean updateUserPassword(PasswordUpdateForm passwordUpdateForm, AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        userAccount.setPassword(passwordService.encryptPassword(passwordUpdateForm.getNewPassword()));
        try {
            userAccount = userAccountService.getRepository().save(userAccount);
        } catch (Exception exception) {
            LOGGER.error("Unable to update user's password.", exception);
            return false;
        }

        appUserService.updateCache(userAccount);

        return true;
    }

    public boolean passwordMatch(PasswordUpdateForm passwordUpdateForm, AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        String currPwd = passwordUpdateForm.getCurrentPassword();
        String encryptCurrPwd = userAccount.getPassword();

        return passwordService.passwordsMatch(currPwd, encryptCurrPwd);
    }

    public AppUser updateUserProfile(UserProfileUpdateForm userProfileUpdateForm, AppUser appUser) {
        UserProfile userProfile = appUserService.retrieveUserInformation(appUser);
        userProfile.setFirstName(userProfileUpdateForm.getFirstName());
        userProfile.setMiddleName(userProfileUpdateForm.getMiddleName());
        userProfile.setLastName(userProfileUpdateForm.getLastName());

        return appUserService.updateUserInformation(userProfile, appUser);
    }

    public UserProfileUpdateForm createUserProfileUpdateForm(AppUser appUser) {
        UserProfile userProfile = appUserService.retrieveUserInformation(appUser);

        UserProfileUpdateForm userProfileUpdateForm = new UserProfileUpdateForm();
        userProfileUpdateForm.setEmail(userProfile.getEmail());
        userProfileUpdateForm.setFirstName(userProfile.getFirstName());
        userProfileUpdateForm.setMiddleName(userProfile.getMiddleName());
        userProfileUpdateForm.setLastName(userProfile.getLastName());

        return userProfileUpdateForm;
    }

}
