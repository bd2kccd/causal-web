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
package edu.pitt.dbmi.ccd.web.mail;

import edu.pitt.dbmi.ccd.mail.service.SimpleMailService;
import javax.mail.MessagingException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Aug 12, 2015 11:32:23 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class BasicWebServiceMail implements SimpleMailService {

    private final String uri;

    private final String appId;

    public BasicWebServiceMail(String uri, String appId) {
        this.uri = uri;
        this.appId = appId;
    }

    @Override
    public void send(String to, String subject, String body, boolean html) throws MessagingException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(uri, new FeedbackRequest(to, body, appId), String.class);
    }

    private class FeedbackRequest {

        private String email;

        private String feedback;

        private String appId;

        public FeedbackRequest() {
        }

        public FeedbackRequest(String email, String feedback, String appId) {
            this.email = email;
            this.feedback = feedback;
            this.appId = appId;
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

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }
    }

}
