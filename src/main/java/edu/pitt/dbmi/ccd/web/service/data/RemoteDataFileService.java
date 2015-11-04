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
package edu.pitt.dbmi.ccd.web.service.data;

import edu.pitt.dbmi.ccd.commons.security.WebSecurityDSA;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.service.RestRequestService;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * Oct 12, 2015 9:14:53 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class RemoteDataFileService implements RestRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDataFileService.class);

    private final String dataRestUrl;

    private final Boolean webapp;

    private final RestTemplate restTemplate;

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public RemoteDataFileService(
            @Value("${ccd.rest.url.data:http://localhost:9000/ccd-ws/api/v1.0/account/{accountId}/data/file}") String dataRestUrl,
            Boolean webapp,
            RestTemplate restTemplate,
            UserAccountService userAccountService) {
        this.dataRestUrl = dataRestUrl;
        this.webapp = webapp;
        this.restTemplate = restTemplate;
        this.userAccountService = userAccountService;
    }

    public Set<String> retrieveDataFileMD5Hash(String username) {
        Set<String> hashes = new HashSet<>();

        if (webapp) {
            return hashes;
        }

        UserAccount userAccount = userAccountService.findByUsername(username);
        String accountId = userAccount.getAccountId();
        if (accountId == null) {
            return hashes;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(this.dataRestUrl)
                    .pathSegment("hash")
                    .buildAndExpand(accountId)
                    .toUri();

            String signature = WebSecurityDSA.createSignature(uri.toString(), userAccount.getPrivateKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HEADER_ACCOUNT, accountId);
            headers.set(HEADER_SIGNATURE, signature);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<DataFileMd5Reponse> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, entity, DataFileMd5Reponse.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                DataFileMd5Reponse reponse = responseEntity.getBody();
                if (reponse != null) {
                    hashes.addAll(reponse.getMd5HashValues());
                }
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return hashes;
    }

    public static class DataFileMd5Reponse {

        private List<String> md5HashValues;

        public DataFileMd5Reponse() {
        }

        public DataFileMd5Reponse(List<String> md5HashValues) {
            this.md5HashValues = md5HashValues;
        }

        public List<String> getMd5HashValues() {
            return md5HashValues;
        }

        public void setMd5HashValues(List<String> md5HashValues) {
            this.md5HashValues = md5HashValues;
        }
    }

}
