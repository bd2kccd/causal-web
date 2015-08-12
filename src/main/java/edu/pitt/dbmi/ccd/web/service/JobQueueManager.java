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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.runtime.Processes;
import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;

/**
 * 
 * Jul 31, 2015 11:19:59 AM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@Service
public class JobQueueManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueManager.class);

    private final JobQueueInfoService jobQueueInfoService;
	private final AlgorithmService algorithmService;

	private final int queueSize;

	/**
	 * @param jobQueueInfoService
	 */
	@Autowired(required = true)
	public JobQueueManager(JobQueueInfoService jobQueueInfoService, AlgorithmService algorithmService,
			@Value("${app.queue.size:1}") final int queueSize) {
		this.jobQueueInfoService = jobQueueInfoService;
		this.algorithmService = algorithmService;
		this.queueSize = queueSize;
	}

	@Scheduled(fixedRate = 5000)
	public void executeJobInQueue() {
		int numRunningJobs = jobQueueInfoService.getNumRunningJobs();
		if (numRunningJobs < queueSize) {
			//System.out.println("numRunningJobs: " + numRunningJobs);
			//int totalJobs = jobQueueInfoService.findAll().size();
			//System.out.println("numWatingInQueueJobs: " + (totalJobs - numRunningJobs));
			//Waiting list to execute
			List<JobQueueInfo> jobList = jobQueueInfoService.findByStatus(new Integer(0));
			if(jobList != null && jobList.size() > 0){
				for(int i=0;i<(queueSize - numRunningJobs);i++){
					JobQueueInfo queuedJobInfo = jobList.get(i);
			        LOGGER.info("Run Job ID: " + queuedJobInfo.getId());
					try {
						algorithmService.runAlgorithmFromQueue(queuedJobInfo.getId(), queuedJobInfo.getCommands(), queuedJobInfo.getFileName(),
								queuedJobInfo.getTmpDirectory(), queuedJobInfo.getOutputDirectory());
						
				        LOGGER.info("Set Job's status to be 1 (running): " + queuedJobInfo.getId());
						queuedJobInfo.setStatus(1);			
						jobQueueInfoService.saveJobIntoQueue(queuedJobInfo);
					} catch (Exception exception) {
						LOGGER.error("Unable to run " + queuedJobInfo.getAlgorName(), exception);
					}
				}
			}
			
		}
		
		//Waiting list to terminate
		List<JobQueueInfo> jobList = jobQueueInfoService.findByStatus(new Integer(2));
		if(jobList != null && jobList.size() > 0){
			for(int i=0;i<(queueSize - numRunningJobs);i++){
				JobQueueInfo queuedJobInfo = jobList.get(i);
				killJob(queuedJobInfo.getId());
			}
		}

	}
	
	private void killJob(Long queueId){
		
		JobQueueInfo jobQueueInfo = jobQueueInfoService.findOne(queueId);
		if(jobQueueInfo.getStatus() == 0){
	        LOGGER.info("Delete Job ID by user from queue: " + queueId);
	        jobQueueInfoService.deleteJobById(queueId);
		}else{
			Long pid = jobQueueInfo.getPid();
			if(pid == null){
		        LOGGER.info("Delete Job ID by user from queue: " + queueId);
		        jobQueueInfoService.deleteJobById(queueId);
			}else{
				Platform platform = Platform.detect();
				System.out.println("Processes.isProcessRunning(platform, pid):" + Processes.isProcessRunning(platform, pid));
				if(Processes.isProcessRunning(platform, pid)){
					/*ISupportConfig support = null;
					IStreamProcessor output = null;*/
					List<String> commands = new LinkedList<>();
					if (platform == Platform.Windows) {
						//return Processes.tryKillProcess(support, platform, output, pid.intValue());
						commands.add("taskkill");
						commands.add("/pid");
						commands.add(String.valueOf(pid));
						commands.add("/f");
						commands.add("/t");
					}else{
						//return Processes.killProcess(support, platform, output, pid.intValue());
						commands.add("kill");
						commands.add("-9");
						commands.add(String.valueOf(pid));
					}
					LOGGER.info("Kill Job Queue Id: " + jobQueueInfo.getId());
					jobQueueInfo.setStatus(2);			
					jobQueueInfoService.saveJobIntoQueue(jobQueueInfo);
					ProcessBuilder pb = new ProcessBuilder(commands);
					try {
						pb.start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Request to kill an algorithm job did not run successfully.", e);
					}
				}else{
			        LOGGER.info("Job does not exist, delete Job ID from queue: " + queueId);
			        jobQueueInfoService.deleteJobById(queueId);
				}
			}
		}
	}
}
