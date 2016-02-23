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

import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.mail.MailService;
import java.util.Base64;
import java.util.UUID;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Feb 23, 2016 5:13:11 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final CcdProperties ccdProperties;
    private final DefaultPasswordService passwordService;
    private final MailService mailService;

    @Autowired
    public UserService(CcdProperties ccdProperties, DefaultPasswordService passwordService, MailService mailService) {
        this.ccdProperties = ccdProperties;
        this.passwordService = passwordService;
        this.mailService = mailService;
    }

    public boolean registerNewUser(final String username, final String password) {
        boolean success = false;

        String accountId = UUID.randomUUID().toString();

        String url = UriComponentsBuilder.fromHttpUrl(ccdProperties.getServerURL()).pathSegment("activate")
                .queryParam("account", Base64.getUrlEncoder().encodeToString(accountId.getBytes()))
                .build().toString();

        return success;
    }

}
