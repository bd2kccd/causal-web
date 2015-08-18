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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

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
    
    /**
	 * @param jobQueueInfoService
	 * @param userAccountService
	 */
	@Autowired(required = true)
	public JobQueueController(JobQueueInfoService jobQueueInfoService, UserAccountService userAccountService) {
		this.jobQueueInfoService = jobQueueInfoService;
		this.userAccountService = userAccountService;
	}

	@RequestMapping(method = RequestMethod.GET)
    public String showJobQueue(@ModelAttribute("appUser") AppUser appUser, 
    		Model model){
		List<AlgorithmJob> listItems = new ArrayList<AlgorithmJob>();

		Optional<UserAccount> userAccount = userAccountService.findByUsername(appUser.getUsername());
		List<JobQueueInfo> listJobs = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount.get()));
		listJobs.forEach(job -> {
			AlgorithmJob algorithmJob = JobQueueUtility.convertJobEntity2JobModel(job);
			listItems.add(algorithmJob);
		});
    	model.addAttribute("jobList", listItems);
    	return JOB_QUEUE;
    }

	@RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
	public String deleteJobQueue(@PathVariable Long id){
		JobQueueInfo job = jobQueueInfoService.findOne(id);
		job.setStatus(2);
		jobQueueInfoService.saveJobIntoQueue(job);
		return REDIRECT_JOB_QUEUE;
	}
	
}
