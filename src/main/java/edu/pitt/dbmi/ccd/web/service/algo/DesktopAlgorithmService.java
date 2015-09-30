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
package edu.pitt.dbmi.ccd.web.service.algo;

import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.DataService;
import edu.pitt.dbmi.ccd.web.service.cloud.dto.AlgorithmJobRequest;
import java.net.URI;
import java.util.Map;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Sep 26, 2015 7:56:49 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopAlgorithmService extends AbstractAlgorithmService implements AlgorithmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopAlgorithmService.class);

    private final String appId;

    private final String algorithmQueueUrl;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public DesktopAlgorithmService(
            @Value("${ccd.rest.appId:1}") String appId,
            @Value("${ccd.rest.url.queue.algorithm}") String algorithmQueueUrl,
            RestTemplate restTemplate,
            DataService dataService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            UserAccountService userAccountService,
            JobQueueInfoService jobQueueInfoService) {
        super(dataService, dataFileService, variableTypeService, userAccountService, jobQueueInfoService);
        this.appId = appId;
        this.algorithmQueueUrl = algorithmQueueUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public void runRemotely(AlgorithmJobRequest jobRequest, AppUser appUser) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(jobRequest, headers);

            URI uri = UriComponentsBuilder.fromHttpUrl(this.algorithmQueueUrl)
                    .pathSegment("submit")
                    .queryParam("usr", appUser.getUsername())
                    .queryParam("appId", this.appId)
                    .build().toUri();
            LOGGER.info(uri.toString());
            restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    @Override
    public void runLocally(String algorithmJar, AlgorithmJobRequest jobRequest, AppUser appUser) {
        LOGGER.info(String.format("Add Job into Queue: %d.", addToLocalQueue(algorithmJar, jobRequest, appUser)));
    }

    @Override
    public Map<String, String> getUserRunnableData(String username) {
        return getUserDataFile(username);
    }

    @Override
    public Map<String, String> getUserRunnableData(String prefix, String username) {
        return getUserDataFile(prefix, username);
    }

}
