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

import edu.pitt.dbmi.ccd.mail.AbstractBasicMail;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 *
 * Feb 23, 2016 5:19:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class MailService extends AbstractBasicMail {

    private static final Locale LOCALE = new Locale("en", "US");

    private static final DateFormat EMAIL_DATE_FORMAT = new SimpleDateFormat("EE, MMMMM dd, yyyy hh:mm:ss a");

    private final String senderEmail;

    private final SpringTemplateEngine templateEngine;

    @Autowired
    public MailService(
            @Value("${spring.mail.username}") String senderEmail,
            SpringTemplateEngine templateEngine,
            JavaMailSender javaMailSender) {
        super(javaMailSender);
        this.senderEmail = senderEmail;
        this.templateEngine = templateEngine;
    }

    public void sendUserActivationLink(String email, String activationLink) throws MessagingException {
        Context context = new Context(LOCALE);
        context.setVariable("email", email);
        context.setVariable("activationLink", activationLink);

        String to = email;
        String subject = "CCD Account Activation";
        String body = this.templateEngine.process("email/userActivation", context);
        send(to, subject, body, true);
    }

    public void sendNewUserAlert(String registeredEmail, Date registrationDate, String userIPAddress) throws MessagingException {
        Context context = new Context(LOCALE);
        context.setVariable("email", registeredEmail);
        context.setVariable("registrationDate", EMAIL_DATE_FORMAT.format(registrationDate));
        context.setVariable("registrationLocation", userIPAddress);

        String to = senderEmail;
        String subject = "New Registered User!";
        String body = this.templateEngine.process("email/newUserAlert", context);
        send(to, subject, body, true);
    }

}
