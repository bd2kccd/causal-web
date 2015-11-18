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

import edu.pitt.dbmi.ccd.queue.service.JobQueueService;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_JOB_QUEUE;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

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
public class JobQueueController implements ViewPath {

    private final JobQueueService jobQueueService;

    @Autowired
    public JobQueueController(JobQueueService jobQueueService) {
        this.jobQueueService = jobQueueService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showJobQueue(
            @ModelAttribute("appUser") AppUser appUser,
            Model model) {
        model.addAttribute("jobList", jobQueueService.createJobQueueList(appUser.getUsername()));

        return JOB_QUEUE;
    }

    @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
    public String deleteJobQueue(@PathVariable Long id) {
        jobQueueService.removeJobQueue(id);

        return REDIRECT_JOB_QUEUE;
    }

}
