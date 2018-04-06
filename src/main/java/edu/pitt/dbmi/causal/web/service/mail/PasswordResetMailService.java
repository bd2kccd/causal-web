/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.service.mail;

import edu.pitt.dbmi.ccd.mail.AbstractTemplateMailService;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 *
 * Oct 6, 2016 12:57:16 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class PasswordResetMailService extends AbstractTemplateMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetMailService.class);

    public PasswordResetMailService(SpringTemplateEngine templateEngine, JavaMailSender mailSender) {
        super(templateEngine, mailSender);
    }

    @Async
    public void sendUserPasswordReset(String email, String resetPasswordURL) {
        Map<String, Object> msgVariables = new HashMap<>();
        msgVariables.put("username", email);
        msgVariables.put("resetPasswordURL", resetPasswordURL);

        String subject = "Causal Web: Account Recovery";
        String template = "mail/account/password_reset";
        try {
            sendMail(msgVariables, template, subject, email);
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send email to notify user of success activation.", exception);
        }
    }

}
