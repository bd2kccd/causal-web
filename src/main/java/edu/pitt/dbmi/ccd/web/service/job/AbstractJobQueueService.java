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

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.queue.model.AlgorithmJob;
import edu.pitt.dbmi.ccd.queue.util.JobQueueUtility;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Sep 24, 2015 3:42:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractJobQueueService {

    protected final JobQueueInfoService jobQueueInfoService;

    protected final UserAccountService userAccountService;

    public AbstractJobQueueService(JobQueueInfoService jobQueueInfoService, UserAccountService userAccountService) {
        this.jobQueueInfoService = jobQueueInfoService;
        this.userAccountService = userAccountService;
    }

    protected List<AlgorithmJob> getLocalAlgorithmJobs(String username) {
        List<AlgorithmJob> algorithmJobs = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);
        List<JobQueueInfo> jobQueueInfos = jobQueueInfoService.findByUserAccounts(Collections.singleton(userAccount));
        jobQueueInfos.forEach(job -> {
            AlgorithmJob algorithmJob = JobQueueUtility.convertJobEntity2JobModel(job);
            algorithmJobs.add(algorithmJob);
        });

        return algorithmJobs;
    }

    protected void removeLocalAlgorithmJobs(Long id) {
        JobQueueInfo job = jobQueueInfoService.findOne(id);
        job.setStatus(2);
        jobQueueInfoService.saveJobIntoQueue(job);
    }

}
