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

import edu.pitt.dbmi.ccd.mail.AbstractBasicMail;
import java.util.Locale;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 *
 * Nov 18, 2015 10:33:13 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class MailService extends AbstractBasicMail {

    private static final Locale LOCALE = new Locale("en", "US");

    private final String sendTo;

    private final SpringTemplateEngine templateEngine;

    @Autowired
    public MailService(
            @Value("${spring.mail.username}") String sendTo,
            SpringTemplateEngine templateEngine,
            JavaMailSender javaMailSender) {
        super(javaMailSender);
        this.sendTo = sendTo;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendRegistrationActivation(String username, String email, String activationUrl) throws MessagingException {
        Context context = new Context(LOCALE);
        context.setVariable("username", username);
        context.setVariable("email", email);
        context.setVariable("activationUrl", activationUrl);

        String to = email;
        String subject = "CCD Account Activation";
        String body = this.templateEngine.process("email/registration-activation", context);
        boolean html = true;
        send(to, subject, body, html);
    }

    @Async
    public void sendFeedback(String email, String feedback) throws MessagingException {
        String feedbackSubject = (email == null || email.trim().isEmpty())
                ? "User Feedback From Anonymous User" : "User Feedback From " + email;
        send(sendTo, feedbackSubject, feedback, false);
    }

}
