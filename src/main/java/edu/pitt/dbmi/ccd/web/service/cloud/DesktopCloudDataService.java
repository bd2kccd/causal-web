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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Aug 17, 2015 11:12:19 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class DesktopCloudDataService implements CloudDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopCloudDataService.class);

    private final String userDataHashUri;

    private final String appId;

    public DesktopCloudDataService(String userDataHashUri, String appId) {
        this.userDataHashUri = userDataHashUri;
        this.appId = appId;
    }

    @Override
    public Set<String> getDataMd5Hash(String username) {
        Set<String> hashes = new HashSet<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            Set<String> set = restTemplate.getForObject(String.format("%s/%s?appId=%s", userDataHashUri, username, appId), Set.class);
            hashes.addAll(set);
        } catch (RestClientException exception) {

        }

        return hashes;
    }

}
