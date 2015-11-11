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

import edu.pitt.dbmi.ccd.ws.dto.mail.FeedbackRequest;
import java.net.URI;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    private final RestTemplate restTemplate;

    @Autowired
    public DesktopMailService(
            @Value("${ccd.rest.url.mail.feedback}") String feedbackUri,
            RestTemplate restTemplate) {
        this.feedbackUri = feedbackUri;
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendRegistrationActivation(String username, String email, String activationUrl) throws MessagingException {
        throw new UnsupportedOperationException("Not supported for desktop application.");
    }

    @Override
    public void sendFeedback(String email, String feedback) throws MessagingException {
        URI uri = UriComponentsBuilder.fromHttpUrl(this.feedbackUri)
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        email = (email == null) ? "" : email.trim();
        FeedbackRequest request = email.isEmpty()
                ? new FeedbackRequest(feedback)
                : new FeedbackRequest(email, feedback);

        HttpEntity<FeedbackRequest> httpEntity = new HttpEntity<>(request, headers);

        restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class);
    }

}
