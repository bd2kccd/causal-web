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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserLoginService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import java.util.Date;
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

    private final UserLoginService userLoginService;

    @Autowired
    public AppUserService(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    public AppUser updateUserProfile(AppUser appUser, Person person) {
        String firstName = person.getFirstName();
        String middleName = person.getMiddleName();
        String lastName = person.getLastName();

        appUser.setFirstName(firstName == null ? "" : firstName);
        appUser.setMiddleName(middleName == null ? "" : middleName);
        appUser.setLastName(lastName == null ? "" : lastName);

        return appUser;
    }

    public AppUser createAppUser(UserAccount userAccount, boolean federatedUser) {
        Person person = userAccount.getPerson();
        String firstName = person.getFirstName();
        String middleName = person.getMiddleName();
        String lastName = person.getLastName();
        String email = person.getEmail();

        UserLogin userLogin = userLoginService.findByUserAccount(userAccount);
        Date lastLoginDate = userLogin.getLastLoginDate();

        AppUser appUser = new AppUser();
        appUser.setUsername(email);
        appUser.setFirstName(firstName == null ? "" : firstName);
        appUser.setMiddleName(middleName == null ? "" : middleName);
        appUser.setLastName(lastName == null ? "" : lastName);
        appUser.setEmail(email);
        appUser.setLastLogin(lastLoginDate == null ? new Date(System.currentTimeMillis()) : lastLoginDate);
        appUser.setFederatedUser(federatedUser);

        return appUser;
    }

}
