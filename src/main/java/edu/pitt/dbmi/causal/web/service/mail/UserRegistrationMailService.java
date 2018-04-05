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

import edu.pitt.dbmi.causal.web.model.mail.UserActivationMail;
import edu.pitt.dbmi.causal.web.model.mail.UserRegistrationAlertMail;
import edu.pitt.dbmi.causal.web.prop.CcdEmailProperties;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserInformation;
import edu.pitt.dbmi.ccd.db.entity.UserRegistration;
import edu.pitt.dbmi.ccd.db.service.UserInformationService;
import edu.pitt.dbmi.ccd.db.service.UserRegistrationService;
import edu.pitt.dbmi.ccd.db.util.InetUtils;
import edu.pitt.dbmi.ccd.mail.AbstractTemplateMailService;
import java.net.URI;
import java.util.Date;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 *
 * Feb 5, 2018 9:42:21 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserRegistrationMailService extends AbstractTemplateMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationMailService.class);

    private final CcdEmailProperties ccdEmailProperties;
    private final UserInformationService userInformationService;
    private final UserRegistrationService userRegistrationService;

    @Autowired
    public UserRegistrationMailService(CcdEmailProperties ccdEmailProperties, UserInformationService userInformationService, UserRegistrationService userRegistrationService, SpringTemplateEngine templateEngine, JavaMailSender javaMailSender) {
        super(templateEngine, javaMailSender);
        this.ccdEmailProperties = ccdEmailProperties;
        this.userInformationService = userInformationService;
        this.userRegistrationService = userRegistrationService;
    }

    /**
     * Send new user registration alert to administrators.
     *
     * @param userAccount
     */
    @Async
    public void sendUserRegistrationAlertToAdmin(UserAccount userAccount) {
        String[] sendTo = ccdEmailProperties.getAdminSendTo();
        if (sendTo.length > 0) {
            UserInformation userInfo = userInformationService.getRepository()
                    .findByUserAccount(userAccount);
            UserRegistration userRegistration = userRegistrationService.getRepository()
                    .findByUserAccount(userAccount);

            String email = userInfo.getEmail();
            Date registrationDate = userRegistration.getRegistrationDate();
            Long location = userRegistration.getRegistrationLocation();

            UserRegistrationAlertMail userRegAlert = new UserRegistrationAlertMail();
            userRegAlert.setEmail(email);
            userRegAlert.setRegistrationDate(registrationDate);
            userRegAlert.setRegistrationLocation(InetUtils.getInetATON(location));

            String subject = "Causal Web: New User Registration";
            String template = "mail/account/user_registration_alert";
            try {
                sendMail(userRegAlert, template, subject, sendTo);
            } catch (MessagingException exception) {
                LOGGER.error("Fail to send new user registration to administrator.", exception);
            }
        }
    }

    /**
     * Email account activation link to user.
     *
     * @param userAccount
     * @param activationLink
     */
    @Async
    public void sendAccountActivationToUser(final UserAccount userAccount, final URI activationLink) {
        UserInformation userInfo = userInformationService.getRepository()
                .findByUserAccount(userAccount);
        UserRegistration userRegistration = userRegistrationService.getRepository()
                .findByUserAccount(userAccount);

        String email = userInfo.getEmail();
        Date registrationDate = userRegistration.getRegistrationDate();

        UserActivationMail userActivation = new UserActivationMail();
        userActivation.setActivationLink(activationLink);
        userActivation.setEmail(email);
        userActivation.setRegistrationDate(registrationDate);

        String sendTo = email;
        String subject = "Causal Web: User Activation";
        String template = "mail/account/user_activation";
        try {
            sendMail(userActivation, template, subject, sendTo);
        } catch (MessagingException exception) {
            LOGGER.error("Fail to send account activation link to user.", exception);
        }
    }

}
