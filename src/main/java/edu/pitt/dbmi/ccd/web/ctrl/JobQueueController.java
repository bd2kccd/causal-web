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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.queue.model.AlgorithmJob;
import edu.pitt.dbmi.ccd.queue.util.JobQueueUtility;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Aug 5, 2015 3:18:44 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/jobQueue")
public class JobQueueController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueController.class);

    private final JobQueueInfoService jobQueueInfoService;

    private final UserAccountService userAccountService;

    private final String algorithmQueueUrl;

    private final String appId;

    private final RestTemplate restTemplate;

    @Autowired(required = true)
    public JobQueueController(
            JobQueueInfoService jobQueueInfoService,
            UserAccountService userAccountService,
            @Value("${ccd.rest.url.queue.algorithm:http://localhost:9000/ccd-ws/queue/algorithm}") String algorithmQueueUrl,
            @Value("${ccd.rest.appId:1}") String appId,
            RestTemplate restTemplate) {
        this.jobQueueInfoService = jobQueueInfoService;
        this.userAccountService = userAccountService;
        this.algorithmQueueUrl = algorithmQueueUrl;
        this.appId = appId;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showJobQueue(
            @ModelAttribute("appUser") AppUser appUser,
            Model model) {
        List<AlgorithmJob> listItems = new ArrayList<>();
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        List<JobQueueInfo> listJobs = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount));
        listJobs.forEach(job -> {
            AlgorithmJob algorithmJob = JobQueueUtility.convertJobEntity2JobModel(job);
            listItems.add(algorithmJob);
        });
        model.addAttribute("jobList", listItems);

        List<AlgorithmJob> remoteListItems = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            URI url = UriComponentsBuilder.fromHttpUrl(this.algorithmQueueUrl)
                    .queryParam("usr", appUser.getUsername())
                    .queryParam("appId", this.appId)
                    .build().toUri();

            ResponseEntity<AlgorithmJob[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, AlgorithmJob[].class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                AlgorithmJob[] algorithmJobs = responseEntity.getBody();
                remoteListItems.addAll(Arrays.asList(algorithmJobs));
            }
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        model.addAttribute("remoteJobList", remoteListItems);

        return JOB_QUEUE;
    }

    @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
    public String deleteJobQueue(@PathVariable Long id) {
        JobQueueInfo job = jobQueueInfoService.findOne(id);
        job.setStatus(2);
        jobQueueInfoService.saveJobIntoQueue(job);

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "/remoteRemove/{id}", method = RequestMethod.GET)
    public String deleteRemoteJobQueue(
            @PathVariable Long id,
            @ModelAttribute("appUser") AppUser appUser,
            Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            URI url = UriComponentsBuilder.fromHttpUrl(this.algorithmQueueUrl + "/" + id)
                    .queryParam("usr", appUser.getUsername())
                    .queryParam("appId", this.appId)
                    .build().toUri();

            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return REDIRECT_JOB_QUEUE;
    }

}
