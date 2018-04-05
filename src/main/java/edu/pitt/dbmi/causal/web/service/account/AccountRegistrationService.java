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

import edu.pitt.dbmi.causal.web.model.account.UserRegistrationForm;
import edu.pitt.dbmi.causal.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.db.domain.account.UserAccountRegistration;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.util.Base64;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 4, 2016 2:49:02 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AccountRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRegistrationService.class);

    private final CcdProperties ccdProperties;
    private final DefaultPasswordService passwordService;
    private final UserAccountService userAccountService;

    public AccountRegistrationService(CcdProperties ccdProperties, DefaultPasswordService passwordService, UserAccountService userAccountService) {
        this.ccdProperties = ccdProperties;
        this.passwordService = passwordService;
        this.userAccountService = userAccountService;
    }

    public boolean activateUserAccount(UserAccount userAccount) {
        userAccount.setActivated(true);
        userAccount.setActionKey(null);
        try {
            userAccountService.getRepository().save(userAccount);
        } catch (Exception exception) {
            LOGGER.error(String.format("Unable to activate user account '%s'.", userAccount.getUsername()), exception);

            return false;
        }

        return true;
    }

    public UserAccount findUserAccount(String activation) {
        String actionKey;
        try {
            actionKey = new String(Base64.getUrlDecoder().decode(activation));
        } catch (IllegalArgumentException exception) {
            return null;
        }

        return userAccountService.getRepository().findByActionKey(actionKey);
    }

    public boolean accountExists(UserRegistrationForm userRegistrationForm) {
        String username = userRegistrationForm.getEmail();

        return userAccountService.getRepository().existsByUsername(username);
    }

    public UserAccount registerRegularAccount(UserRegistrationForm userRegistrationForm, String ipAddress, boolean federatedUser) {
        UserAccountRegistration registration = toAccountRegistration(userRegistrationForm, ipAddress, federatedUser);

        return userAccountService.registerRegularUser(registration);
    }

    private UserAccountRegistration toAccountRegistration(UserRegistrationForm userRegistrationForm, String ipAddress, boolean federatedUser) {
        String email = userRegistrationForm.getEmail();
        String password = passwordService.encryptPassword(userRegistrationForm.getPassword());
        String firstName = userRegistrationForm.getFirstName();
        String middleName = userRegistrationForm.getMiddleName();
        String lastName = userRegistrationForm.getLastName();
        boolean activated = !ccdProperties.isRequireActivation() || federatedUser;

        UserAccountRegistration registration = new UserAccountRegistration(email, password);
        registration.setActivated(activated);
        registration.setFirstName(firstName);
        registration.setMiddleName(middleName);
        registration.setLastName(lastName);
        registration.setIpAddress(ipAddress);

        return registration;
    }

}
