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
package edu.pitt.dbmi.ccd.web.service.user.settings;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.service.RestRequestService;
import edu.pitt.dbmi.ccd.ws.dto.user.account.DesktopWebAccountRequest;
import edu.pitt.dbmi.ccd.ws.dto.user.account.DesktopWebAccountResponse;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Oct 8, 2015 2:42:11 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopUserSettingsService implements UserSettingsService, RestRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopUserSettingsService.class);

    private final String userAccountUrl;

    private final String appId;

    private final RestTemplate restTemplate;

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public DesktopUserSettingsService(
            @Value("${ccd.rest.url.user.account}") String userAccountUrl,
            @Value("${ccd.rest.appId}") String appId,
            RestTemplate restTemplate,
            UserAccountService userAccountService) {
        this.userAccountUrl = userAccountUrl;
        this.appId = appId;
        this.restTemplate = restTemplate;
        this.userAccountService = userAccountService;
    }

    @Override
    public boolean deleteWebAcount(String username) {
        boolean success = false;

        UserAccount userAccount = userAccountService.findByUsername(username);
        userAccount.setAccountId(null);
        try {
            userAccountService.saveUserAccount(userAccount);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        return success;
    }

    @Override
    public boolean addWebAccount(String username, String password, String desktopUsername) {
        boolean success = false;

        String plainCreds = username + ":" + password;
        String base64Creds = Base64.getEncoder().encodeToString(plainCreds.getBytes());

        UserAccount userAccount = userAccountService.findByUsername(desktopUsername);
        String publicKey = userAccount.getPublicKey();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add(HEADER_AUTH, "Basic " + base64Creds);

            HttpEntity<DesktopWebAccountRequest> entity = new HttpEntity<>(new DesktopWebAccountRequest(publicKey), headers);

            URI uri = UriComponentsBuilder.fromHttpUrl(this.userAccountUrl)
                    .pathSegment("desktop")
                    .buildAndExpand(username).toUri();

            ResponseEntity<DesktopWebAccountResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, DesktopWebAccountResponse.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                DesktopWebAccountResponse response = responseEntity.getBody();
                if (response != null) {
                    userAccount.setAccountId(response.getAccountId());
                    try {
                        userAccountService.saveUserAccount(userAccount);
                        success = true;
                    } catch (Exception exception) {
                        exception.printStackTrace(System.err);
                        LOGGER.error(exception.getMessage());
                    }
                }
            }
        } catch (RestClientException exception) {
            exception.printStackTrace(System.err);
            LOGGER.error(exception.getMessage());
        }

        return success;
    }

}
