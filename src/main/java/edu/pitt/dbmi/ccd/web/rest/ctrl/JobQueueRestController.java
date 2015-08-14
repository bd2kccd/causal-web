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
package edu.pitt.dbmi.ccd.web.rest.ctrl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.AlgorithmJob;
import edu.pitt.dbmi.ccd.web.service.JobQueueService;

/**
 * 
 * Aug 13, 2015 11:23:18 AM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping("/{username}/jobQueue")
public class JobQueueRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueRestController.class);

	private final UserAccountService userAccountService;

	private final JobQueueService jobQueueService;

	private final JobQueueInfoService jobQueueInfoService;
	
	/**
	 * @param userAccountService
	 * @param jobQueueService
	 * @param jobQueueInfoService
	 */
	@Autowired(required = true)
	public JobQueueRestController(UserAccountService userAccountService, JobQueueService jobQueueService,
			JobQueueInfoService jobQueueInfoService) {
		this.userAccountService = userAccountService;
		this.jobQueueService = jobQueueService;
		this.jobQueueInfoService = jobQueueInfoService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<AlgorithmJob> showJobQueue(@PathVariable String username) {
		validateUser(username);
		return jobQueueService.createJobQueueList(username);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public AlgorithmJob readSingleJobQueueInfo(@PathVariable String username, @PathVariable Long id) {
		validateUser(username);
		JobQueueInfo job = jobQueueInfoService.findOne(id);
		return convertJobEntity2JobModel(job);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?>  addJobQueue(@PathVariable String username,
			@RequestBody JobQueueInfo job,
			//@RequestParam(value = "algorName") String algorName,
			//@RequestParam(value = "command") String command, @RequestParam(value = "fileName") String fileName,
			@ModelAttribute("appUser") AppUser appUser) {
		validateUser(username);
		Optional<UserAccount> userAccount = userAccountService.findByUsername(username);
		//JobQueueInfo job = new JobQueueInfo(null, algorName, command, fileName, appUser.getTmpDirectory(),
		//		appUser.getOutputDirectory(), new Integer(0), new Date(System.currentTimeMillis()),
		//		Collections.singleton(userAccount.get()));
		job.setTmpDirectory(appUser.getTmpDirectory());
		job.setOutputDirectory(appUser.getOutputDirectory());
		job.setStatus(new Integer(0));
		job.setAddedTime(new Date(System.currentTimeMillis()));
		job.setUserAccounts(Collections.singleton(userAccount.get()));
		job = jobQueueInfoService.saveJobIntoQueue(job);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(ServletUriComponentsBuilder
				.fromCurrentRequest().path("/{id}")
				.buildAndExpand(job.getId()).toUri());
        LOGGER.info("Add Job into Queue: " + job.getId());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
	public AlgorithmJob removeJobQueue(@PathVariable String username, @PathVariable Long id) {
		validateUser(username);
		return jobQueueService.removeJobQueue(id);
	}

	private AlgorithmJob convertJobEntity2JobModel(JobQueueInfo job){
		return new AlgorithmJob(job.getId(), job.getAlgorName(), job.getFileName(),
				(job.getStatus().intValue() == 0 ? "Queued" : (job.getStatus().intValue() == 1 ? "Running" : "Kill Request")),
				FilePrint.fileTimestamp(job.getAddedTime().getTime()));
	}
	
	private void validateUser(String username) {
		userAccountService.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
	}
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5982276223679242219L;

	public UserNotFoundException(String username) {
		super("Could not find user '" + username + "'.");
	}
}