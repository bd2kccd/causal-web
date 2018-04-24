/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.ctrl.job;

import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.ccd.db.domain.job.JobQueueListItem;
import edu.pitt.dbmi.ccd.db.entity.JobInfo;
import edu.pitt.dbmi.ccd.db.entity.JobQueue;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobQueueService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Apr 12, 2018 1:50:12 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/ws/job/queue")
public class JobQueueRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueRestController.class);

    private final AppUserService appUserService;
    private final JobQueueService jobQueueService;

    @Autowired
    public JobQueueRestController(AppUserService appUserService, JobQueueService jobQueueService) {
        this.appUserService = appUserService;
        this.jobQueueService = jobQueueService;
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> cancelJob(@PathVariable final Long id, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        JobQueue jobQueue = jobQueueService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (jobQueue == null) {
            return ResponseEntity.notFound().build();
        }

        jobQueueService.setStatusCanceled(jobQueue);

        return ResponseEntity.ok(toJobQueueListItem(jobQueue));
    }

    @GetMapping
    public ResponseEntity<?> list(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        return ResponseEntity.ok(jobQueueService.getRepository()
                .getJobQueueListItems(userAccount));
    }

    public JobQueueListItem toJobQueueListItem(JobQueue jobQueue) {
        JobInfo jobInfo = jobQueue.getJobInfo();

        Long id = jobQueue.getId();
        String name = jobInfo.getName();
        Date creationTime = jobInfo.getCreationTime();
        String status = jobInfo.getJobStatus().getName();
        String location = jobInfo.getJobLocation().getName();

        return new JobQueueListItem(id, name, creationTime, status, location);
    }

}
