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

    public BasicWebServiceMail(String uri) {
        this.uri = uri;
    }

    @Override
    public void send(String to, String subject, String body, boolean html) throws MessagingException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(uri, new BasicMail(to, subject, body), String.class);
    }

    private class BasicMail {

        private String to;

        private String subject;

        private String body;

        public BasicMail() {
        }

        public BasicMail(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

}
