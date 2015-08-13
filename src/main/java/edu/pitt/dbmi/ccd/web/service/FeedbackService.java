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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.mail.service.SimpleMailService;
import edu.pitt.dbmi.ccd.web.model.Feedback;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 12, 2015 9:00:35 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FeedbackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackService.class);

    private final SimpleMailService simpleMailService;

    @Autowired(required = true)
    public FeedbackService(SimpleMailService simpleMailService) {
        this.simpleMailService = simpleMailService;
    }

    public void sendFeedback(final Feedback feedback) {
        String email = feedback.getEmail();
        String feedbackMsg = feedback.getFeedbackMsg();

        Thread t = new Thread(() -> {
            try {
                simpleMailService.send(email, null, feedbackMsg, false);
            } catch (MessagingException exception) {
                LOGGER.error(exception.getMessage());
            }
        });
        t.start();
    }

}
