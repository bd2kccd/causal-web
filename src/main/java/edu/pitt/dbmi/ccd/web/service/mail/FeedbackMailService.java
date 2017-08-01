/*
 * Copyright (C) 2017 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.web.model.FeedbackForm;
import edu.pitt.dbmi.ccd.web.prop.CcdEmailProperties;
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
 * Oct 5, 2016 5:43:12 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FeedbackMailService extends AbstractMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackMailService.class);

    @Autowired
    public FeedbackMailService(CcdEmailProperties ccdEmailProperties, SpringTemplateEngine templateEngine, JavaMailSender javaMailSender) {
        super(ccdEmailProperties, templateEngine, javaMailSender);
    }

    @Async
    public void sendFeedback(FeedbackForm feedbackForm) {
        String[] sendTo = ccdEmailProperties.getAdminSendTo();
        if (sendTo.length > 0) {
            if (feedbackForm.getEmail() == null) {
                feedbackForm.setEmail("anonymous");
            }

            String subject = "Causal Web: User Feedback";
            String template = "mail/feedback";
            try {
                sendMail(feedbackForm, template, subject, sendTo);
            } catch (MessagingException exception) {
                LOGGER.error("Fail to send new user registration to administrator.", exception);
            }
        }
    }

}
