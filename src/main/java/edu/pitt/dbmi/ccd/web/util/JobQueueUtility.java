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
package edu.pitt.dbmi.ccd.web.util;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.queue.model.AlgorithmJob;

/**
 * 
 * Aug 18, 2015 2:52:28 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
public class JobQueueUtility {

	public static AlgorithmJob convertJobEntity2JobModel(JobQueueInfo job){
		return new AlgorithmJob(job.getId(), job.getAlgorName(), job.getFileName(),
				(job.getStatus().intValue() == 0 ? "Queued" : (job.getStatus().intValue() == 1 ? "Running" : "Kill Request")),
				FilePrint.fileTimestamp(job.getAddedTime().getTime()));
	}
	
}
