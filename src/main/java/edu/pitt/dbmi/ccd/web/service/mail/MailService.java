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
package edu.pitt.dbmi.ccd.web.service.mail;

import edu.pitt.dbmi.ccd.mail.service.BasicMailService;
import edu.pitt.dbmi.ccd.mail.service.UserBasicMailService;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 23, 2016 5:19:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class MailService {

    private final BasicMailService basicMailService;

    private final UserBasicMailService userBasicMailService;

    @Autowired
    public MailService(BasicMailService basicMailService, UserBasicMailService userBasicMailService) {
        this.basicMailService = basicMailService;
        this.userBasicMailService = userBasicMailService;
    }

    public void sendRegistrationActivation(String username, String email, String activationUrl) throws MessagingException {
        userBasicMailService.sendRegistrationActivation(username, email, activationUrl);
    }

}
