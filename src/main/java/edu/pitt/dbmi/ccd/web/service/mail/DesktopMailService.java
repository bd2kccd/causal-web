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

import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Aug 16, 2015 4:22:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopMailService implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopMailService.class);

    private final String feedbackUri;

    private final String appId;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public DesktopMailService(
            @Value("${ccd.rest.url.mail.feedback}") String feedbackUri,
            @Value("${ccd.rest.appId}") String appId,
            RestTemplate restTemplate) {
        this.feedbackUri = feedbackUri;
        this.appId = appId;
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendRegistrationActivation(String username, String email, String activationUrl) throws MessagingException {
        throw new UnsupportedOperationException("Not supported for desktop application.");
    }

    @Override
    public void sendFeedback(String email, String feedback) throws MessagingException {
        String uri = feedbackUri + "?appId=" + appId;
        if (email == null) {
            restTemplate.postForEntity(uri, new FeedbackRequest(feedback), String.class);
        } else {
            restTemplate.postForEntity(uri, new FeedbackRequest(email, feedback), String.class);
        }
    }

    private class FeedbackRequest {

        private String email;

        private String feedback;

        public FeedbackRequest() {
        }

        public FeedbackRequest(String email, String feedback) {
            this.email = email;
            this.feedback = feedback;
        }

        public FeedbackRequest(String feedback) {
            this.feedback = feedback;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

    }

}
