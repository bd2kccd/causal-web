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
package edu.pitt.dbmi.causal.web.service;

import edu.pitt.dbmi.causal.web.exception.InternalErrorException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.UserInfoForm;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserInfo;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(cacheNames = {"appUserServiceUserAccount"}, key = "#appUser.username")
    public UserAccount retrieveUserAccount(AppUser appUser) {
        try {
            return userAccountService.getRepository().findByUsername(appUser.getUsername());
        } catch (Exception exception) {
            LOGGER.error("Unable to retrieve user account.", exception);
            throw new InternalErrorException();
        }
    }

    @CachePut(cacheNames = {"appUserServiceUserAccount"}, key = "#userAccount.username")
    public UserAccount updateCache(UserAccount userAccount) {
        return userAccount;
    }

    public AppUser create(UserAccount userAccount, boolean federatedUser) {
        userAccount = updateCache(userAccount);

        UserInfo userInfo = userAccount.getUserInfo();
        UserLogin userLogin = userAccount.getUserLogin();

        String firstName = userInfo.getFirstName();
        String middleName = userInfo.getMiddleName();
        String lastName = userInfo.getLastName();
        String username = userAccount.getUsername();
        Date lastLogin = userLogin.getLoginDate();

        AppUser appUser = update(firstName, middleName, lastName, new AppUser());
        appUser.setUsername(username);
        appUser.setLastLogin((lastLogin == null) ? new Date(System.currentTimeMillis()) : lastLogin);
        appUser.setFederatedUser(federatedUser);

        return appUser;
    }

    public AppUser update(UserInfoForm userInfoForm, AppUser appUser) {
        String firstName = userInfoForm.getFirstName();
        String middleName = userInfoForm.getMiddleName();
        String lastName = userInfoForm.getLastName();

        return update(firstName, middleName, lastName, appUser);
    }

    public AppUser update(UserInfo userInfo, AppUser appUser) {
        String firstName = userInfo.getFirstName();
        String middleName = userInfo.getMiddleName();
        String lastName = userInfo.getLastName();

        return update(firstName, middleName, lastName, appUser);
    }

    public AppUser update(String firstName, String middleName, String lastName, AppUser appUser) {
        appUser.setFirstName(firstName == null ? "" : firstName);
        appUser.setMiddleName(middleName == null ? "" : middleName);
        appUser.setLastName(lastName == null ? "" : lastName);

        return appUser;
    }

}
