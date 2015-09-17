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
package edu.pitt.dbmi.ccd.web.service.mail;

import edu.pitt.dbmi.ccd.mail.service.BasicMailService;
import edu.pitt.dbmi.ccd.mail.service.UserBasicMailService;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 16, 2015 4:22:37 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("server")
@Service
public class ServerMailService implements MailService {

    private final String sendTo;

    private final String subject;

    private final BasicMailService basicMailService;

    private final UserBasicMailService userBasicMailService;

    @Autowired(required = true)
    public ServerMailService(
            @Value("${ccd.mail.feedback.to}") String sendTo,
            @Value("${ccd.mail.feedback.subject:User Feedback}") String subject,
            BasicMailService basicMailService,
            UserBasicMailService userBasicMailService) {
        this.sendTo = sendTo;
        this.subject = subject;
        this.basicMailService = basicMailService;
        this.userBasicMailService = userBasicMailService;
    }

    @Override
    public void sendRegistrationActivation(String username, String email, String activationUrl) throws MessagingException {
        userBasicMailService.sendRegistrationActivation(username, email, activationUrl);
    }

    @Override
    public void sendFeedback(String email, String feedback) throws MessagingException {
        String feedbackSubject = (email == null || email.trim().isEmpty())
                ? subject + ": Feedback From Anonymous User" : subject + ": Feedback From " + email;
        basicMailService.send(sendTo, feedbackSubject, feedback, false);
    }

}
