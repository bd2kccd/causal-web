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

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.account.PasswordResetForm;
import edu.pitt.dbmi.ccd.web.domain.account.PasswordResetRequestForm;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.net.URI;
import java.util.Base64;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Apr 20, 2017 4:40:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserAccountService userAccountService;
    private final DefaultPasswordService passwordService;

    @Autowired
    public PasswordResetService(UserAccountService userAccountService, DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.passwordService = passwordService;
    }

    public boolean isValidActivationKey(String activation) {
        String activationKey = new String(Base64.getUrlDecoder().decode(activation));

        return userAccountService.getRepository().existsByActionKey(activationKey);
    }

    public boolean changePassword(PasswordResetForm passwordReset) {
        String activationKey = new String(Base64.getUrlDecoder().decode(passwordReset.getActivationKey()));
        String password = passwordReset.getPassword();

        UserAccount userAccount = userAccountService.getRepository().findByActionKey(activationKey);
        if (userAccount == null) {
            return false;
        }

        userAccount.setPassword(passwordService.encryptPassword(password));
        userAccount.setActionKey(null);
        try {
            userAccountService.getRepository().save(userAccount);
        } catch (Exception exception) {
            LOGGER.error("Unable to reset password for user.", exception);
            return false;
        }

        return true;
    }

    public URI createPasswordResetLink(UserAccount userAccount, HttpServletRequest req) {
        URI uri = null;

        try {
            userAccount.setActionKey(UUID.randomUUID().toString());
            userAccountService.getRepository().save(userAccount);

            String actionKey = userAccount.getActionKey();
            uri = UriTool.buildURI(req)
                    .pathSegment("user", "account", "password", "reset")
                    .queryParam("activation", Base64.getUrlEncoder().encodeToString(actionKey.getBytes()))
                    .build().toUri();
        } catch (Exception exception) {
            LOGGER.error(String.format("Unable to save activation key to reset password for '%s'.", userAccount.getUsername()), exception);
        }

        return uri;
    }

    public UserAccount retrieveUserAccount(PasswordResetRequestForm passwordResetRequestForm) {
        String username = passwordResetRequestForm.getEmailToResetPassword();

        return userAccountService.getRepository().findByUsername(username);
    }

}
