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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.JobInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Apr 12, 2018 3:36:53 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/ws/job/info")
public class JobInfoRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobInfoRestController.class);

    private final AppUserService appUserService;
    private final JobInfoService jobInfoService;

    @Autowired
    public JobInfoRestController(AppUserService appUserService, JobInfoService jobInfoService) {
        this.appUserService = appUserService;
        this.jobInfoService = jobInfoService;
    }

    @GetMapping("history")
    public ResponseEntity<?> list(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        return ResponseEntity.ok(jobInfoService.getRepository()
                .getJobInfoListItems(userAccount));
    }

}
