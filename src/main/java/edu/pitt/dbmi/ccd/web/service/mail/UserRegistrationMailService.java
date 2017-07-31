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

import edu.pitt.dbmi.ccd.commons.uri.InetUtils;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.model.template.UserRegistrationTemplateData;
import edu.pitt.dbmi.ccd.web.prop.CcdEmailProperties;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.net.URI;
import java.util.Date;
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
    public void sendUserRegistrationAlertToAdmin(final UserAccount userAccount) {
        String[] sendTo = ccdEmailProperties.getAdminSendTo();
        if (sendTo.length > 0) {
            String email = userAccount.getUserInfo().getEmail();
            Date registrationDate = userAccount.getRegistrationDate();
            Long location = userAccount.getRegistrationLocation();

            UserRegistrationTemplateData templateData = new UserRegistrationTemplateData();
            templateData.setEmail(email);
            templateData.setRegistrationDate(registrationDate);
            templateData.setRegistrationLocation(UriTool.ipAddressToHostName(InetUtils.getInetATON(location)));

            String subject = "Causal Web: New User Registration";
            String template = "mail/account/registration/userRegistrationAlert";
            try {
                sendMail(templateData, template, subject, sendTo);
            } catch (MessagingException exception) {
                LOGGER.error("Fail to send new user registration to administrator.", exception);
            }
        }
    }

    @Async
    public void sendAccountActivationToUser(final UserAccount userAccount, final URI activationLink) {
        String email = userAccount.getUserInfo().getEmail();
        Date registrationDate = userAccount.getRegistrationDate();

        UserRegistrationTemplateData templateData = new UserRegistrationTemplateData();
        templateData.setActivationLink(activationLink);
        templateData.setEmail(email);
        templateData.setRegistrationDate(registrationDate);

        String sendTo = email;
        String subject = "Causal Web: User Activation";
        String template = "mail/account/registration/userActivationRequest";
        try {
            sendMail(templateData, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Fail to send account activation link to user.", exception);
        }
    }

}
