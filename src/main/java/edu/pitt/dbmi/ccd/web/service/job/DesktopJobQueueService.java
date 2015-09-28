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
package edu.pitt.dbmi.ccd.web.service.job;

import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.queue.model.AlgorithmJob;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
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
 * Sep 24, 2015 3:30:21 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class DesktopJobQueueService extends AbstractJobQueueService implements JobQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopJobQueueService.class);

    private final String jobQueueUrl;

    private final String appId;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public DesktopJobQueueService(
            @Value("${ccd.rest.url.queue.algorithm}") String jobQueueUrl,
            @Value("${ccd.rest.appId}") String appId,
            RestTemplate restTemplate,
            JobQueueInfoService jobQueueInfoService,
            UserAccountService userAccountService) {
        super(jobQueueInfoService, userAccountService);
        this.jobQueueUrl = jobQueueUrl;
        this.appId = appId;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<AlgorithmJob> listLocalAlgorithmJobs(String username) {
        return getLocalAlgorithmJobs(username);
    }

    @Override
    public List<AlgorithmJob> listRemoteAlgorithmJobs(String username) {
        List<AlgorithmJob> algorithmJobs = new LinkedList<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromHttpUrl(this.jobQueueUrl)
                    .queryParam("usr", username)
                    .queryParam("appId", this.appId)
                    .build().toUri();

            ResponseEntity<AlgorithmJob[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, AlgorithmJob[].class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                AlgorithmJob[] jobs = responseEntity.getBody();
                algorithmJobs.addAll(Arrays.stream(jobs).collect(Collectors.toList()));
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return algorithmJobs;
    }

    @Override
    public void deleteLocalAlgorithmJobs(Long id) {
        removeLocalAlgorithmJobs(id);
    }

    @Override
    public void deleteRemoteAlgorithmJobs(Long id, String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            URI uri = UriComponentsBuilder.fromHttpUrl(this.jobQueueUrl)
                    .pathSegment(id.toString())
                    .queryParam("usr", username)
                    .queryParam("appId", this.appId)
                    .build().toUri();
            restTemplate.exchange(uri, HttpMethod.DELETE, entity, String.class);
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

}
