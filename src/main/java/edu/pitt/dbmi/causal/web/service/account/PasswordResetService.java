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
package edu.pitt.dbmi.causal.web.service.account;

import edu.pitt.dbmi.causal.web.service.mail.PasswordResetMailService;
import edu.pitt.dbmi.causal.web.util.UriTool;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
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
 * Oct 6, 2016 12:54:36 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserAccountService userAccountService;
    private final PasswordResetMailService passwordResetMailService;
    private final DefaultPasswordService passwordService;

    @Autowired
    public PasswordResetService(UserAccountService userAccountService, PasswordResetMailService passwordResetMailService, DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.passwordResetMailService = passwordResetMailService;
        this.passwordService = passwordService;
    }

    public boolean proccessPasswordResetRequestForm(UserAccount userAccount, String newPassword) {
        userAccount.setPassword(passwordService.encryptPassword(newPassword));
        userAccount.setActionKey(null);
        try {
            userAccountService.getRepository()
                    .save(userAccount);
        } catch (Exception exception) {
            LOGGER.error("Unable to reset password for user.", exception);

            return false;
        }

        return true;
    }

    public boolean proccessPasswordResetRequest(UserAccount userAccount, HttpServletRequest req) {
        String activationKey = UUID.randomUUID().toString();
        String resetPasswordURL = UriTool.buildURI(req)
                .pathSegment("user", "account", "password", "reset", "form")
                .queryParam("reset", Base64.getUrlEncoder().encodeToString(activationKey.getBytes()))
                .build().toString();

        try {
            userAccount.setActionKey(activationKey);
            userAccount = userAccountService.getRepository()
                    .save(userAccount);
        } catch (Exception exception) {
            LOGGER.error(String.format("Unable to save activation key to reset password for '%s'.", userAccount.getUsername()), exception);

            return false;
        }

        passwordResetMailService.sendUserPasswordReset(userAccount.getUsername(), resetPasswordURL);

        return true;
    }

}
