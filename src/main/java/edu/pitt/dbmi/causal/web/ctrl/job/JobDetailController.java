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

import edu.pitt.dbmi.causal.web.ctrl.SitePaths;
import edu.pitt.dbmi.causal.web.ctrl.SiteViews;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.job.JobDetailForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.job.JobService;
import edu.pitt.dbmi.ccd.db.entity.JobDetail;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobDetailService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jul 25, 2018 11:13:56 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/job/detail")
public class JobDetailController {

    private final JobService jobService;
    private final JobDetailService jobDetailService;
    private final AppUserService appUserService;

    @Autowired
    public JobDetailController(JobService jobService, JobDetailService jobDetailService, AppUserService appUserService) {
        this.jobService = jobService;
        this.jobDetailService = jobDetailService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping("{id}")
    public String updateJobDetailInfo(
            @Valid @ModelAttribute("jobDetailForm") final JobDetailForm jobDetailForm,
            final BindingResult bindingResult,
            @PathVariable final Long id,
            final Model model,
            final AppUser appUser,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.jobDetailForm", bindingResult);
            redirAttrs.addFlashAttribute("jobDetailForm", jobDetailForm);
            redirAttrs.addFlashAttribute("errorMsg", true);

            return SitePaths.REDIRECT_JOB_DETAIL + "/" + id;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        JobDetail jobDetail = jobDetailService.getRepository().findByIdAndUserAccount(id, userAccount);

        jobService.updateJobDetailInfo(jobDetailForm, jobDetail, userAccount);

        return SitePaths.REDIRECT_JOB_DETAIL + "/" + id;
    }

    @GetMapping("{id}")
    public String showJobDetails(@PathVariable final Long id, final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        JobDetail jobDetail = jobDetailService.getRepository().findByIdAndUserAccount(id, userAccount);

        if (!model.containsAttribute("jobDetailForm")) {
            model.addAttribute("jobDetailForm", jobService.createJobDetailForm(jobDetail));
        }
        model.addAttribute("jobDetail", jobDetail);
        model.addAttribute("submissionDetails", jobService.getJobSubmissionDetails(jobDetail));

        return SiteViews.JOB_DETAIL;
    }

}
