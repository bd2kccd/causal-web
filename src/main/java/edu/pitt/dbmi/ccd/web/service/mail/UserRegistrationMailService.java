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

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.prop.CcdEmailProperties;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 *
 * Oct 4, 2016 4:04:08 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserRegistrationMailService extends AbstractMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationMailService.class);

    @Autowired
    public UserRegistrationMailService(CcdEmailProperties ccdEmailProperties, SpringTemplateEngine templateEngine, JavaMailSender javaMailSender) {
        super(ccdEmailProperties, templateEngine, javaMailSender);
    }

    @Async
    public void sendAdminNewUserRegistrationNotification(final UserAccount userAccount) {
        String email = userAccount.getPerson().getEmail();
        Date registrationDate = userAccount.getCreatedDate();

        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("registrationDate", registrationDate);

        String[] sendTo = ccdEmailProperties.getAdminSendTo();
        String subject = "Causal Web: New User Registration";
        String template = "email/account/registration/admin_new_registration_notification";
        try {
            sendMail(variables, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to email new user activation link.", exception);
        }
    }

    @Async
    public void sendUserSelfActivation(final UserAccount userAccount, String activationLink) {
        String email = userAccount.getPerson().getEmail();
        Date registrationDate = userAccount.getCreatedDate();

        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("registrationDate", registrationDate);
        variables.put("activationLink", activationLink);

        String sendTo = email;
        String subject = "Causal Web: User Activation";
        String template = "email/account/registration/user_activation";
        try {
            sendMail(variables, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to email new user activation link.", exception);
        }
    }

}
