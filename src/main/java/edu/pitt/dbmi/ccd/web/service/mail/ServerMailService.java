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

import edu.pitt.dbmi.ccd.mail.service.SimpleMailService;
import edu.pitt.dbmi.ccd.mail.service.UserMailService;
import javax.mail.MessagingException;

/**
 *
 * Aug 16, 2015 4:22:37 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class ServerMailService implements MailService {

    private final String sendTo;

    private final String subject;

    private final SimpleMailService simpleMailService;

    private final UserMailService userMailService;

    public ServerMailService(String sendTo, String subject, SimpleMailService simpleMailService, UserMailService userMailService) {
        this.sendTo = sendTo;
        this.subject = subject;
        this.simpleMailService = simpleMailService;
        this.userMailService = userMailService;
    }

    @Override
    public void sendRegistrationActivation(String username, String email, String activationUrl) throws MessagingException {
        userMailService.sendRegistrationActivation(username, email, activationUrl);
    }

    @Override
    public void sendFeedback(String email, String feedback) throws MessagingException {
        String feedbackSubject = (email == null || email.trim().isEmpty())
                ? subject + ": Feedback From Anonymous User" : subject + ": Feedback From " + email;

        simpleMailService.send(sendTo, feedbackSubject, feedback, false);
    }

}
