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
import edu.pitt.dbmi.ccd.web.conf.prop.CcdEmailProperties;
import java.util.Collections;
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
 * May 26, 2016 12:16:13 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserRegistrationMailService extends AbstractMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationMailService.class);

    @Autowired
    public UserRegistrationMailService(SpringTemplateEngine templateEngine, CcdEmailProperties ccdEmailProperties, JavaMailSender javaMailSender) {
        super(templateEngine, ccdEmailProperties, javaMailSender);
    }

    @Async
    public void sendUserActivationSuccess(final UserAccount userAccount, String url) {
        Map<String, String> msgVariables = new HashMap<>();
        msgVariables.put("username", userAccount.getUsername());
        msgVariables.put("url", url);

        String sendTo = userAccount.getPerson().getEmail();
        String subject = ccdEmailProperties.getAcctRegUserActivSuccessSubject();
        String template = "email/account/registration/user_activated";
        try {
            sendMail(msgVariables, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send email to notify user of success activation.", exception);
        }
    }

    @Async
    public void sendUserSelfActivation(final UserAccount userAccount, String activationLink) {
        String sendTo = userAccount.getPerson().getEmail();
        String subject = ccdEmailProperties.getAcctRegUserActivSubject();
        String template = "email/account/registration/user_self_activation";

        Map<String, String> msgVariables = new HashMap<>();
        msgVariables.put("username", userAccount.getUsername());
        msgVariables.put("activationLink", activationLink);

        try {
            sendMail(msgVariables, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send user self-activation emai.", exception);
        }
    }

    @Async
    public void sendUserNewAccountConfirmation(final UserAccount userAccount) {
        String sendTo = userAccount.getPerson().getEmail();
        String subject = ccdEmailProperties.getAcctRegUserConfirmSubject();
        Map<String, String> msgVariables = Collections.singletonMap("username", userAccount.getUsername());
        String template = "email/account/registration/user_confirmation";
        try {
            sendMail(msgVariables, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send user registration confirmation emai.", exception);
        }
    }

    @Async
    public void sendAdminUserActivation(final UserAccount userAccount, String activationLink) {
        String[] emailTo = ccdEmailProperties.getAdminSendTo();
        String subject = ccdEmailProperties.getAcctRegAdminActivSubject();
        Map<String, String> msgVariables = createUserActivationMsgVariables(userAccount, activationLink);
        String template = "email/account/registration/user_activation";
        try {
            sendMail(msgVariables, template, subject, emailTo);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to email new user activation link.", exception);
        }
    }

    private Map<String, String> createUserActivationMsgVariables(UserAccount userAccount, String activationLink) {
        String email = userAccount.getPerson().getEmail();
        String registrationDate = EMAIL_DATE_FORMAT.format(userAccount.getRegistrationDate());

        Map<String, String> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("registrationDate", registrationDate);
        variables.put("activationLink", activationLink);

        return variables;
    }

}
