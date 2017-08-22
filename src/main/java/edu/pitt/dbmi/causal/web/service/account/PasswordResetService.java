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
package edu.pitt.dbmi.causal.web.service.account;

import edu.pitt.dbmi.causal.web.model.account.PasswordResetForm;
import edu.pitt.dbmi.causal.web.model.account.PasswordResetRequestForm;
import edu.pitt.dbmi.causal.web.util.UriTool;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
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

    public boolean resetPassword(String activation, PasswordResetForm passwordReset) {
        String activationKey = new String(Base64.getUrlDecoder().decode(activation));
        UserAccount userAccount = userAccountService.getRepository().findByActionKey(activationKey);
        if (userAccount == null) {
            return false;
        }

        try {
            userAccount.setPassword(passwordService.encryptPassword(passwordReset.getPassword()));
            userAccount.setActionKey(null);
            userAccountService.getRepository().save(userAccount);

            return true;
        } catch (Exception exception) {
            LOGGER.error("Unable to reset password for user.", exception);
            return false;
        }
    }

    public boolean isValidActivationKey(String activation) {
        String activationKey = new String(Base64.getUrlDecoder().decode(activation));

        return userAccountService.getRepository().existsByActionKey(activationKey);
    }

    public URI createPasswordResetLink(UserAccount userAccount, HttpServletRequest req) {
        String actionKey = UUID.randomUUID().toString();
        try {
            userAccount.setActionKey(actionKey);
            userAccountService.getRepository().save(userAccount);
        } catch (Exception exception) {
            LOGGER.error(String.format("Unable to save activation key to reset password for '%s'.", userAccount.getUsername()), exception);
            actionKey = null;
        }

        return (actionKey == null)
                ? null
                : UriTool.buildURI(req)
                        .pathSegment("user", "account", "password", "reset")
                        .queryParam("activation", Base64.getUrlEncoder().encodeToString(actionKey.getBytes()))
                        .build().toUri();
    }

    public UserAccount retrieveUserAccount(PasswordResetRequestForm passwordResetRequestForm) {
        String username = passwordResetRequestForm.getEmail();

        return userAccountService.getRepository().findByUsername(username);
    }

}
