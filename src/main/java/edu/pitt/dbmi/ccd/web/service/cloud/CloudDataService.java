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
package edu.pitt.dbmi.ccd.web.service.cloud;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Aug 21, 2015 9:36:34 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class CloudDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudDataService.class);

    private final Boolean webapp;

    private final String userDataHashUri;

    private final String appId;

    @Autowired(required = true)
    public CloudDataService(
            Boolean webapp,
            @Value("${ccd.data.usr.hash.uri:http://localhost:8080/ccd-ws/data/usr}") String userDataHashUri,
            @Value("${ccd.rest.appId:1}") String appId) {
        this.webapp = webapp;
        this.userDataHashUri = userDataHashUri;
        this.appId = appId;
    }

    public Set<String> getDataMd5Hash(String username) {
        Set<String> hashes = new HashSet<>();

        if (!webapp) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Set<String> set = restTemplate.getForObject(String.format("%s/%s?appId=%s", userDataHashUri, username, appId), Set.class);
                hashes.addAll(set);
            } catch (RestClientException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        return hashes;
    }

}
