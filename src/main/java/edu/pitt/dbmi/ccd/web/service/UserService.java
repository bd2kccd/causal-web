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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.mail.MailService;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.mail.MessagingException;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    private final DefaultPasswordService passwordService;

    private final MailService mailService;

    @Autowired(required = true)
    public UserService(
            @Value("${ccd.server.url:}") String serverUrl,
            UserAccountService userAccountService,
            DefaultPasswordService passwordService,
            MailService mailService) {
        this.serverUrl = serverUrl;
        this.userAccountService = userAccountService;
        this.passwordService = passwordService;
        this.mailService = mailService;
    }

    public void registerNewUser(
            final UserRegistration userRegistration,
            final String workspace,
            final String requestURL) throws Exception {
        if (userRegistration == null) {
            throw new IllegalArgumentException("No user registration given.");
        }

        String username = userRegistration.getUsername();
        String email = userRegistration.getEmail();
        String password = userRegistration.getPassword();

        String accountId = UUID.randomUUID().toString();

        String httpUrl = serverUrl.isEmpty() ? requestURL : serverUrl + (new URI(requestURL)).getPath();

        String url = UriComponentsBuilder.fromHttpUrl(httpUrl)
                .pathSegment("activate")
                .queryParam("account", Base64.getUrlEncoder().encodeToString(accountId.getBytes()))
                .build().toString();

        Person person = new Person();
        person.setFirstName("");
        person.setLastName("");
        person.setEmail(email);
        person.setWorkspace(Paths.get(workspace).toString());

        UserAccount userAccount = new UserAccount();
        userAccount.setAccountId(accountId);
        userAccount.setActive(Boolean.FALSE);
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
        userAccount.setPassword(passwordService.encryptPassword(password));
        userAccount.setPerson(person);
        userAccount.setUsername(username);

        userAccountService.saveUserAccount(userAccount);
        Thread t = new Thread(() -> {
            try {
                mailService.sendRegistrationActivation(username, email, url);
            } catch (MessagingException exception) {
                LOGGER.warn(String.format("Unable to send registration email for user '%s'.", username), exception);
            }
        });
        t.start();
    }

}
