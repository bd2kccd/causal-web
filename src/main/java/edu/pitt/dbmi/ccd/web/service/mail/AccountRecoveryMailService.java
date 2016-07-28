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

import edu.pitt.dbmi.ccd.web.prop.CcdEmailProperties;
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
 * May 30, 2016 10:50:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AccountRecoveryMailService extends AbstractMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRecoveryMailService.class);

    @Autowired
    public AccountRecoveryMailService(SpringTemplateEngine templateEngine, CcdEmailProperties ccdEmailProperties, JavaMailSender javaMailSender) {
        super(templateEngine, ccdEmailProperties, javaMailSender);
    }

    @Async
    public void sendUserPasswordRecovery(String email, String resetPasswordURL) {
        Map<String, String> msgVariables = new HashMap<>();
        msgVariables.put("username", email);
        msgVariables.put("resetPasswordURL", resetPasswordURL);

        String subject = ccdEmailProperties.getAcctPwdResetSubject();
        String template = "email/account/recovery/password_recovery";
        try {
            sendMail(msgVariables, template, subject, email);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send email to notify user of success activation.", exception);
        }
    }

}
