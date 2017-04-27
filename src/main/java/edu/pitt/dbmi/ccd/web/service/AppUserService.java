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
package edu.pitt.dbmi.ccd.web.service;

import com.auth0.Auth0User;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserInfo;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.account.UserInfoForm;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 5, 2015 5:51:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AppUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserService.class);

    private final UserAccountService userAccountService;

    @Autowired
    public AppUserService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public UserAccount retrieveUserAccount(AppUser appUser) {
        UserAccount userAccount = null;

        String username = appUser.getUsername();
        try {
            userAccount = userAccountService.getUserAccountRepository().findByUsername(username);
        } catch (Exception exception) {
            LOGGER.error("Unable to retrieve user account.", exception);
        }

        return userAccount;
    }

    public AppUser update(UserInfoForm userInfoForm, AppUser appUser) {
        String firstName = userInfoForm.getFirstName();
        String middleName = userInfoForm.getMiddleName();
        String lastName = userInfoForm.getLastName();

        appUser.setFirstName((firstName == null) ? "" : firstName);
        appUser.setMiddleName((middleName == null) ? "" : middleName);
        appUser.setLastName((lastName == null) ? "" : lastName);

        return appUser;
    }

    public AppUser update(UserInfo userInfo, AppUser appUser) {
        String firstName = userInfo.getFirstName();
        String middleName = userInfo.getMiddleName();
        String lastName = userInfo.getLastName();

        appUser.setFirstName(firstName == null ? "" : firstName);
        appUser.setMiddleName(middleName == null ? "" : middleName);
        appUser.setLastName(lastName == null ? "" : lastName);

        return appUser;
    }

    public AppUser create(Auth0User auth0User) {
        String firstName = auth0User.getGivenName();
        String lastName = auth0User.getFamilyName();
        String email = auth0User.getEmail().toLowerCase();

        AppUser appUser = new AppUser();
        appUser.setUsername(email);
        appUser.setFirstName((firstName == null) ? "" : firstName);
        appUser.setMiddleName("");
        appUser.setLastName((lastName == null) ? "" : lastName);
        appUser.setFederatedUser(Boolean.TRUE);

        return appUser;
    }

    public AppUser create(UserAccount userAccount, boolean federatedUser) {
        UserInfo userInfo = userAccount.getUserInfo();
        UserLogin userLogin = userAccount.getUserLogin();

        String username = userAccount.getUsername();
        String firstName = userInfo.getFirstName();
        String middleName = userInfo.getMiddleName();
        String lastName = userInfo.getLastName();
        Date lastLogin = userLogin.getLoginDate();

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setFirstName(firstName == null ? "" : firstName);
        appUser.setMiddleName(middleName == null ? "" : middleName);
        appUser.setLastName(lastName == null ? "" : lastName);
        appUser.setLastLogin((lastLogin == null) ? new Date(System.currentTimeMillis()) : lastLogin);
        appUser.setFederatedUser(federatedUser);

        return appUser;
    }

}
