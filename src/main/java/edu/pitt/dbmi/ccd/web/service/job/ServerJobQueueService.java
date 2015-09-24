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
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 24, 2015 3:30:54 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("server")
@Service
public class ServerJobQueueService extends AbstractJobQueueService implements JobQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerJobQueueService.class);

    @Autowired(required = true)
    public ServerJobQueueService(JobQueueInfoService jobQueueInfoService, UserAccountService userAccountService) {
        super(jobQueueInfoService, userAccountService);
    }

    @Override
    public List<AlgorithmJob> listLocalAlgorithmJobs(String username) {
        return getLocalAlgorithmJobs(username);
    }

    @Override
    public List<AlgorithmJob> listRemoteAlgorithmJobs(String username) {
        return new LinkedList<>();
    }

    @Override
    public void deleteLocalAlgorithmJobs(Long id) {
        removeLocalAlgorithmJobs(id);
    }

    @Override
    public void deleteRemoteAlgorithmJobs(Long id, String username) {
    }

}
