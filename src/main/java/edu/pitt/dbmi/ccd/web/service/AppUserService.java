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
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final CcdProperties ccdProperties;
    private final UserLoginService userLoginService;

    @Autowired
    public AppUserService(CcdProperties ccdProperties, UserLoginService userLoginService) {
        this.ccdProperties = ccdProperties;
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

    public AppUser createAppUser(final UserAccount userAccount, final boolean federatedUser) {
        UserLogin userLogin = userLoginService.findByUserAccount(userAccount);
        Date lastLoginDate = userLogin.getLastLoginDate();

        Person person = userAccount.getPerson();
        String firstName = person.getFirstName();
        String middleName = person.getMiddleName();
        String lastName = person.getLastName();
        String email = person.getEmail();
        String workspace = person.getWorkspace();

        String dataFolder = ccdProperties.getDataFolder();
        String tmpFolder = ccdProperties.getTmpFolder();
        String resultFolder = ccdProperties.getResultFolder();

        Path[] directories = {
            Paths.get(workspace, dataFolder),
            Paths.get(workspace, resultFolder),
            Paths.get(workspace, tmpFolder),
            Paths.get(workspace, resultFolder, "algorithm"),
            Paths.get(workspace, resultFolder, "comparison")
        };
        for (Path directory : directories) {
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to create directory '%s'.", directory), exception);
                }
            }
        }

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
