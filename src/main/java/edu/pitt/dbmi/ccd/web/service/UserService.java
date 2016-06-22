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

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.SecurityAnswer;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.SecurityAnswerService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.mail.MailService;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import javax.mail.MessagingException;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

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

        String username = userRegistration.getUsername();
        String email = userRegistration.getEmail();
        String password = userRegistration.getPassword();

        String accountId = UUID.randomUUID().toString();

        try {
            UriComponentsBuilder uriComponentsBuilder = serverUrl.isEmpty()
                    ? UriComponentsBuilder.fromHttpUrl(requestURL)
                    : UriComponentsBuilder.fromHttpUrl(serverUrl).pathSegment((new URI(requestURL)).getPath());

            String url = uriComponentsBuilder
                    .pathSegment("activate")
                    .queryParam("account", Base64.getUrlEncoder().encodeToString(accountId.getBytes()))
                    .build().toString();

            Person person = new Person();
            person.setFirstName("");
            person.setLastName("");
            person.setEmail(email);
            person.setWorkspace("");

            UserAccount userAccount = new UserAccount(person, username, passwordService.encryptPassword(password), accountId);

            SecurityAnswer securityAnswer = new SecurityAnswer();
            securityAnswer.setAnswer(userRegistration.getSecureAns());
            securityAnswer.setSecurityQuestion(userRegistration.getSecureQues());
            securityAnswer.setUserAccounts(Collections.singleton(userAccount));

            success = persistUserRegistration(userAccount, securityAnswer);
            if (success) {
                Thread t = new Thread(() -> {
                    try {
                        mailService.sendRegistrationActivation(username, email, url);
                    } catch (MessagingException exception) {
                        LOGGER.warn(String.format("Unable to send registration email for user '%s'.", username), exception);
                    }
                });
                t.start();
            }
        } catch (Exception exception) {
            LOGGER.warn(exception.getMessage());
        }

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
