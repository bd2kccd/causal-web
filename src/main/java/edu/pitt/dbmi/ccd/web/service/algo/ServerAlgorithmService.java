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
import edu.pitt.dbmi.ccd.web.service.cloud.dto.JobRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 26, 2015 7:57:02 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("server")
@Service
public class ServerAlgorithmService extends AbstractAlgorithmService implements AlgorithmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerAlgorithmService.class);

    @Autowired(required = true)
    public ServerAlgorithmService(
            DataService dataService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            UserAccountService userAccountService,
            JobQueueInfoService jobQueueInfoService) {
        super(dataService, dataFileService, variableTypeService, userAccountService, jobQueueInfoService);
    }

    @Override
    public void runRemotely(JobRequest jobRequest, AppUser appUser) {
        throw new UnsupportedOperationException("Not supported in server mode.");
    }

    @Override
    public void runLocally(String algorithm, String algorithmJar, JobRequest jobRequest, AppUser appUser) {
        LOGGER.info(String.format("Add Job into Queue: %d.", addToLocalQueue(algorithm, algorithmJar, jobRequest, appUser)));
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
