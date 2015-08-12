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
package edu.pitt.dbmi.ccd.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.AlgorithmJob;

/**
 * 
 * Aug 5, 2015 1:44:27 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@Service
public class JobQueueService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueService.class);

	private final JobQueueInfoService jobQueueInfoService;

	private final UserAccountService userAccountService;

	/**
	 * @param jobQueueInfoService
	 * @param userAccountService
	 */
	@Autowired(required = true)
	public JobQueueService(JobQueueInfoService jobQueueInfoService, UserAccountService userAccountService) {
		this.jobQueueInfoService = jobQueueInfoService;
		this.userAccountService = userAccountService;
	}

	public List<AlgorithmJob> createJobQueueList(String username) {
		List<AlgorithmJob> listItems = new ArrayList<AlgorithmJob>();

		UserAccount userAccount = userAccountService.findByUsername(username);
		List<JobQueueInfo> listJobs = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount));
		listJobs.forEach(job -> {
			AlgorithmJob algorithmJob = new AlgorithmJob(job.getId(), job.getAlgorName(), job.getFileName(),
					(job.getStatus().intValue() == 0 ? "Queued" : (job.getStatus().intValue() == 1 ? "Running" : "Kill Request")),
					FilePrint.fileTimestamp(job.getAddedTime().getTime()));
			listItems.add(algorithmJob);
		});
		return listItems;
	}
	
	public void removeJobQueue(Long queueId){
		JobQueueInfo jobQueueInfo = jobQueueInfoService.findOne(queueId);
		jobQueueInfo.setStatus(2);
		jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);
	}
}
