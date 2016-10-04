/*
 * Copyright (C) 2015 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.db.entity.SecurityAnswer;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.SecurityAnswerService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.mail.MailService;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Aug 4, 2015 9:27:56 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final String serverUrl;

    private final UserAccountService userAccountService;

    private final SecurityAnswerService securityAnswerService;

    private final DefaultPasswordService passwordService;

    private final MailService mailService;

    @Autowired
    public UserService(
            @Value("${ccd.server.url:}") String serverUrl,
            UserAccountService userAccountService,
            SecurityAnswerService securityAnswerService,
            DefaultPasswordService passwordService,
            MailService mailService) {
        this.serverUrl = serverUrl;
        this.userAccountService = userAccountService;
        this.securityAnswerService = securityAnswerService;
        this.passwordService = passwordService;
        this.mailService = mailService;
    }

    public boolean registerNewUser(
            final UserRegistration userRegistration,
            final String requestURL) {
        boolean success = false;

        return success;
    }

    @Transactional(noRollbackFor = Exception.class)
    private boolean persistUserRegistration(UserAccount userAccount, SecurityAnswer securityAnswer) throws Exception {
        boolean flag = false;

        try {
            userAccountService.saveUserAccount(userAccount);
            securityAnswerService.saveSecurityAnswer(securityAnswer);
            flag = true;
        } catch (Exception exception) {
            throw new Exception("Unable to create new user account.");
        }

        return flag;
    }

}
