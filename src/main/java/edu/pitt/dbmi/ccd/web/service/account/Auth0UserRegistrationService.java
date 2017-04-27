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

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.account.UserRegistrationForm;
import edu.pitt.dbmi.ccd.web.util.PasswordUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 29, 2016 4:07:58 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Service
public class Auth0UserRegistrationService {

    public UserRegistrationForm createUserRegistrationForm(AppUser appUser) {
        String username = appUser.getUsername();
        String firstName = appUser.getFirstName();
        String lastName = appUser.getLastName();
        String password = PasswordUtils.generatePassword(20);

        UserRegistrationForm userRegistrationForm = new UserRegistrationForm(true);
        userRegistrationForm.setRegisterEmail(username);
        userRegistrationForm.setFirstName(firstName);
        userRegistrationForm.setLastName(lastName);
        userRegistrationForm.setRegisterPassword(password);
        userRegistrationForm.setConfirmRegisterPassword(password);

        return userRegistrationForm;
    }

}
