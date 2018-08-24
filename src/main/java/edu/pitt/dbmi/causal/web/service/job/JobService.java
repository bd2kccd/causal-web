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
package edu.pitt.dbmi.causal.web.service.job;

import edu.pitt.dbmi.causal.web.model.job.JobDetailForm;
import edu.pitt.dbmi.causal.web.util.DateFormatUtils;
import edu.pitt.dbmi.ccd.db.entity.JobDetail;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.JobDetailService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 22, 2018 4:30:38 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    private final Pattern PIPE_PATTERN = Pattern.compile("\\|");
    private final Pattern COLON_PATTERN = Pattern.compile(":");

    private final JobDetailService jobDetailService;
    private final FileService fileService;
    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;

    @Autowired
    public JobService(JobDetailService jobDetailService, FileService fileService, FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService) {
        this.jobDetailService = jobDetailService;
        this.fileService = fileService;
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
    }

    public Map<String, String> getJobSubmissionDetails(JobDetail jobDetail) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Name", jobDetail.getName());
        details.put("Created Date", DateFormatUtils.format(jobDetail.getCreationTime()));
        details.put("Start Date", DateFormatUtils.format(jobDetail.getStartTime()));
        details.put("End Date", DateFormatUtils.format(jobDetail.getEndTime()));
        details.put("Algorithm", jobDetail.getAlgorithmType().getName());
        details.put("Status", jobDetail.getJobStatus().getName());

        return details;
    }

    public Map<String, String> getJobParameters(JobDetail jobDetail) {
        Map<String, String> params = new LinkedHashMap<>();

        Arrays.stream(PIPE_PATTERN.split(jobDetail.getJobParameter()))
                .forEach(e -> {
                    String[] keyVal = COLON_PATTERN.split(e);
                    if (keyVal.length == 2) {
                        String key = keyVal[0];
                        String val = keyVal[1];
                        params.put(key, (val.equals("true") ? "yes" : val.equals("false") ? "no" : val));
                    }
                });

        return params;
    }

    public JobDetail updateJobDetailInfo(JobDetailForm jobDetailForm, JobDetail jobDetail, UserAccount userAccount) {
        if (jobDetail != null) {
            jobDetail.setName(jobDetailForm.getName());
            jobDetail.setDescription(jobDetailForm.getDescription());
            try {
                jobDetail = jobDetailService.getRepository().save(jobDetail);
            } catch (Exception exception) {
                LOGGER.error("Unable to update job detail information.", exception);
            }
        }

        return jobDetail;
    }

    public JobDetailForm createJobDetailForm(JobDetail jobDetail) {
        JobDetailForm form = new JobDetailForm();
        form.setName(jobDetail.getName());
        form.setDescription(jobDetail.getDescription());

        return form;
    }

}
