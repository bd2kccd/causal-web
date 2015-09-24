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

import edu.pitt.dbmi.ccd.queue.model.AlgorithmJob;
import java.util.List;

/**
 *
 * Sep 24, 2015 3:29:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public interface JobQueueService {

    public List<AlgorithmJob> listLocalAlgorithmJobs(String username);

    public List<AlgorithmJob> listRemoteAlgorithmJobs(String username);

    public void deleteLocalAlgorithmJobs(Long id);

    public void deleteRemoteAlgorithmJobs(Long id, String username);

}
