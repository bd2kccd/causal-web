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
package edu.pitt.dbmi.ccd.web.service.user;

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.util.PasswordTool;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 29, 2016 4:07:58 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Service
public class Auth0UserRegistrationService {

    private final UserRegistrationService userRegistrationService;

    @Autowired
    public Auth0UserRegistrationService(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    public void registerNewUser(AppUser appUser, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String username = appUser.getEmail();
        String firstName = appUser.getFirstName();
        String lastName = appUser.getLastName();
        String password = PasswordTool.generatePassword(20);

        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setAgree(true);
        userRegistration.setConfirmPassword(password);
        userRegistration.setFirstName(firstName);
        userRegistration.setLastName(lastName);
        userRegistration.setPassword(password);
        userRegistration.setUsername(username);

        userRegistrationService.registerNewRegularUser(userRegistration, redirectAttributes, request);
    }

}
