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
package edu.pitt.dbmi.ccd.web.service.account;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.account.PasswordRecovery;
import edu.pitt.dbmi.ccd.web.model.account.PasswordReset;
import edu.pitt.dbmi.ccd.web.service.mail.AccountRecoveryMailService;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.util.Base64;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 6, 2016 12:54:36 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AccountRecoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRecoveryService.class);

    private static final String[] UNACTIVATED_ACCOUNT = {"Login Failed!", "Your account has not been activated."};
    private static final String[] USER_NOT_FOUND = {"Password Reset Failed!", "No such user found."};

    private final UserAccountService userAccountService;
    private final AccountRecoveryMailService accountRecoveryMailService;
    private final DefaultPasswordService passwordService;

    @Autowired
    public AccountRecoveryService(UserAccountService userAccountService, AccountRecoveryMailService accountRecoveryMailService, DefaultPasswordService passwordService) {
        this.userAccountService = userAccountService;
        this.accountRecoveryMailService = accountRecoveryMailService;
        this.passwordService = passwordService;
    }

    public void resetPassword(PasswordReset passwordReset, RedirectAttributes redirectAttributes) {
        String activationKey = passwordReset.getActivationKey();
        String password = passwordReset.getPassword();

        UserAccount userAccount = userAccountService.findByActivationKey(activationKey);
        userAccount.setPassword(passwordService.encryptPassword(password));
        userAccount.setActivationKey(null);
        try {
            userAccountService.save(userAccount);

            redirectAttributes.addFlashAttribute("header", "Password Reset Success!");
            redirectAttributes.addFlashAttribute("successMsg", "You have successfully reset your password.");
        } catch (Exception exception) {
            LOGGER.error("Unable to reset password for user.", exception);

            redirectAttributes.addFlashAttribute("header", "Password Reset Failed!");
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to reset password.");
        }
    }

    public boolean isValidActivationKey(String activationKey) {
        return (userAccountService.findByActivationKey(activationKey) != null);
    }

    public void recoverPasswordRequest(PasswordRecovery passwordRecovery, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String email = passwordRecovery.getUsernameRecover();
        UserAccount userAccount = userAccountService.findByEmail(email);
        if (userAccount == null) {
            redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);
            redirectAttributes.addFlashAttribute("errorMsg", USER_NOT_FOUND);
        } else if (!userAccount.getActive()) {
            redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);
            redirectAttributes.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);
        } else {
            String activationKey = UUID.randomUUID().toString();
            String resetPasswordURL = UriTool.buildURI(request)
                    .pathSegment("user", "account", "recovery", "password")
                    .queryParam("reset", Base64.getUrlEncoder().encodeToString(activationKey.getBytes()))
                    .build().toString();
            try {
                userAccount.setActivationKey(activationKey);
                userAccount = userAccountService.save(userAccount);

                accountRecoveryMailService.sendUserPasswordRecovery(email, resetPasswordURL);

                redirectAttributes.addFlashAttribute("successMsg", "E-mail for password reset has been sent out.");
            } catch (Exception exception) {
                LOGGER.error(String.format("Unable to save activation key to reset password for '%s'.", userAccount.getUsername()), exception);
                redirectAttributes.addFlashAttribute("passwordRecovery", passwordRecovery);
                redirectAttributes.addFlashAttribute("errorMsg", String.format("Unable to reset password for '%s'.", userAccount.getUsername()));
            }
        }
    }

}
