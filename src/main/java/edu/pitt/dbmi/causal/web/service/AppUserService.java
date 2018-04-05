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
package edu.pitt.dbmi.causal.web.service;

import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserInformation;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserInformationService;
import edu.pitt.dbmi.ccd.db.service.UserLoginService;
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
    private final UserInformationService userInformationService;
    private final UserLoginService userLoginService;

    @Autowired
    public AppUserService(UserAccountService userAccountService, UserInformationService userInformationService, UserLoginService userLoginService) {
        this.userAccountService = userAccountService;
        this.userInformationService = userInformationService;
        this.userLoginService = userLoginService;
    }

    public AppUser create(UserAccount userAccount, boolean federatedUser, String ipAddress) {

        // clear any account related request if user successfully signed
        userAccountService.clearActionKey(userAccount);

        userAccount = updateCache(userAccount);

        UserLogin userLogin = userLoginService
                .logUserLogin(userAccount, ipAddress);
        UserInformation userInfo = userInformationService.getRepository()
                .findByUserAccount(userAccount);

        AppUser appUser = new AppUser();
        appUser.setFirstName(userInfo.getFirstName());
        appUser.setMiddleName(userInfo.getMiddleName());
        appUser.setLastName(userInfo.getLastName());
        appUser.setUsername(userAccount.getUsername());
        appUser.setLastLogin(userLogin.getLoginDate());
        appUser.setFederatedUser(federatedUser);

        return appUser;
    }

    public AppUser updateUserInformation(UserInformation userInfo, AppUser appUser) {
        try {
            userInformationService.getRepository().save(userInfo);
        } catch (Exception exception) {
            LOGGER.error("Unable to update user information.", exception);

            return null;
        }

        appUser.setFirstName(userInfo.getFirstName());
        appUser.setMiddleName(userInfo.getMiddleName());
        appUser.setLastName(userInfo.getLastName());

        appUser.updateFullName();

        return appUser;
    }

    public UserInformation retrieveUserInformation(AppUser appUser) {
        UserAccount userAccount = retrieveUserAccount(appUser);

        return userInformationService.getRepository()
                .findByUserAccount(userAccount);
    }

    @Cacheable(cacheNames = {"appUserServiceUserAccount"}, key = "#appUser.username")
    public UserAccount retrieveUserAccount(AppUser appUser) {
        UserAccount userAccount;
        try {
            userAccount = userAccountService.getRepository()
                    .findByUsername(appUser.getUsername());
        } catch (Exception exception) {
            LOGGER.error("Unable to retrieve user account.", exception);
            userAccount = null;
        }

        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        return userAccount;
    }

    @CachePut(cacheNames = {"appUserServiceUserAccount"}, key = "#userAccount.username")
    public UserAccount updateCache(UserAccount userAccount) {
        return userAccount;
    }

}
