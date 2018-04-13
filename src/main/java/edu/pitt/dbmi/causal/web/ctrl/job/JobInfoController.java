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

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.job.JobInfoCtrlService;
import edu.pitt.dbmi.ccd.db.entity.JobInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.AlgorithmTypeService;
import edu.pitt.dbmi.ccd.db.service.JobInfoService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Apr 12, 2018 3:36:18 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/job/info")
public class JobInfoController {

    private final AppUserService appUserService;
    private final JobInfoService jobInfoService;
    private final JobInfoCtrlService jobInfoCtrlService;

    @Autowired
    public JobInfoController(AppUserService appUserService, JobInfoService jobInfoService, JobInfoCtrlService jobInfoCtrlService) {
        this.appUserService = appUserService;
        this.jobInfoService = jobInfoService;
        this.jobInfoCtrlService = jobInfoCtrlService;
    }

    @GetMapping("{id}")
    public String showJobInfo(@PathVariable final Long id, final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        JobInfo jobInfo = jobInfoService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (jobInfo == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("title", jobInfo.getName());
        model.addAttribute("generalInfo", jobInfoCtrlService.getGeneralInfo(jobInfo));
        model.addAttribute("dataset", jobInfoCtrlService.getFiles(jobInfo.getDatasetId(), jobInfo.isSingleDataset()));

        boolean isTetradJob = AlgorithmTypeService.TETRAD_SHORT_NAME
                .equals(jobInfo.getAlgorithmType().getShortName());
        if (isTetradJob) {
            Map<String, String> params = jobInfoCtrlService.parseParameters(jobInfo.getAlgoParam());
            model.addAttribute("algoInfo", jobInfoCtrlService.getAlgoInfos(params));
            model.addAttribute("algoParams", jobInfoCtrlService.getAlgoParameters(params));
        }

        return ViewPath.JOB_INFO_VIEW;
    }

    @GetMapping("history")
    public String showJobInfoHistory() {
        return ViewPath.JOB_INFO_HISTORY_VIEW;
    }

}
