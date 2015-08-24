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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.queue.model.AlgorithmJob;
import edu.pitt.dbmi.ccd.queue.util.JobQueueUtility;
import edu.pitt.dbmi.ccd.web.domain.AppUser;

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
public class JobQueueController implements ViewController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueController.class);

	private final JobQueueInfoService jobQueueInfoService;

	private final UserAccountService userAccountService;

	private final String remoteServerUrl;

	/**
	 * @param jobQueueInfoService
	 * @param userAccountService
	 * @param remoteServerUrl
	 */
	@Autowired(required = true)
	public JobQueueController(JobQueueInfoService jobQueueInfoService, UserAccountService userAccountService,
			@Value("${ccd.remote.server:http://localhost:9000/ccd-ws}") String remoteServerUrl) {
		this.jobQueueInfoService = jobQueueInfoService;
		this.userAccountService = userAccountService;
		this.remoteServerUrl = remoteServerUrl;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showJobQueue(@ModelAttribute("appUser") AppUser appUser, Model model)
					throws UnsupportedEncodingException, MalformedURLException, IOException {
		List<AlgorithmJob> listItems = new ArrayList<AlgorithmJob>();

		Optional<UserAccount> userAccount = userAccountService.findByUsername(appUser.getUsername());
		List<JobQueueInfo> listJobs = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount.get()));
		listJobs.forEach(job -> {
			AlgorithmJob algorithmJob = JobQueueUtility.convertJobEntity2JobModel(job);
			listItems.add(algorithmJob);
		});
		model.addAttribute("jobList", listItems);

		// Remote Job Queue
		String userRemoteServerUrl = remoteServerUrl + "/" + appUser.getUsername() + "/jobQueue";
		List<AlgorithmJob> remoteListItems = new ArrayList<AlgorithmJob>();

		/*RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<AlgorithmJob[]> response = restTemplate.getForEntity(userRemoteServerUrl, AlgorithmJob[].class);
		remoteListItems.addAll(Arrays.asList(response.getBody()));*/
		
		JsonReader reader = new JsonReader(new InputStreamReader((new URL(userRemoteServerUrl)).openStream(), "UTF-8"));
		
		reader.beginArray();
		Gson gson = new Gson();
		while (reader.hasNext()) {
			AlgorithmJob job = gson.fromJson(reader, AlgorithmJob.class);
			remoteListItems.add(job);
		}
		reader.endArray();
		reader.close();
		
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
	public String deleteRemoteJobQueue(@ModelAttribute("appUser") AppUser appUser, Model model, @PathVariable Long id)
					throws IOException {

		String remoteRemoveUrl = remoteServerUrl + "/" + appUser.getUsername() + "/jobQueue/remove/" + id;
		URL url = new URL(remoteRemoveUrl);
		// HttpsURLConnection
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		// Will post appId later
		conn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

		return REDIRECT_JOB_QUEUE;
	}

}
