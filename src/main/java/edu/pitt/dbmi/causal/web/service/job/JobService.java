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

import edu.pitt.dbmi.causal.web.model.ParamOption;
import edu.pitt.dbmi.causal.web.model.job.JobDetailForm;
import edu.pitt.dbmi.causal.web.model.job.JobFile;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithm;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradParams;
import edu.pitt.dbmi.causal.web.tetrad.TetradScore;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTest;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.causal.web.util.DateFormatUtils;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.JobDetail;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.TetradJob;
import edu.pitt.dbmi.ccd.db.entity.TetradVariableFile;
import edu.pitt.dbmi.ccd.db.service.JobDetailService;
import edu.pitt.dbmi.ccd.db.service.TetradJobService;
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
    private final TetradJobService tetradJobService;

    @Autowired
    public JobService(JobDetailService jobDetailService, TetradJobService tetradJobService) {
        this.jobDetailService = jobDetailService;
        this.tetradJobService = tetradJobService;
    }

    public Map<String, String> getAlgorithmParameters(TetradJob tetradJob) {
        Map<String, String> details = new LinkedHashMap<>();

        TetradParams tetradParams = TetradParams.getInstance();
        Arrays.stream(PIPE_PATTERN.split(tetradJob.getAlgorithmParameter()))
                .forEach(e -> {
                    String[] keyVal = COLON_PATTERN.split(e);
                    if (keyVal.length == 2) {
                        String key = keyVal[0];
                        String val = keyVal[1];

                        ParamOption paramOption = tetradParams.getParamOption(key);
                        String desc = (paramOption == null) ? key : paramOption.getText();

                        details.put(desc, (val.equals("true") ? "yes" : val.equals("false") ? "no" : val));
                    }
                });

        return details;
    }

    public Map<String, String> getAlgorithms(TetradJob tetradJob) {
        Map<String, String> details = new LinkedHashMap<>();

        Arrays.stream(PIPE_PATTERN.split(tetradJob.getAlgorithm()))
                .forEach(e -> {
                    String[] keyVal = COLON_PATTERN.split(e);
                    if (keyVal.length == 2) {
                        String key = keyVal[0];
                        String val = keyVal[1];
                        switch (key) {
                            case "algorithm":
                                TetradAlgorithm algorithm = TetradAlgorithms.getInstance().getTetradAlgorithm(val);
                                if (algorithm != null) {
                                    details.put("Algorithm", algorithm.getAlgorithm().getAnnotation().name());
                                }
                                break;
                            case "score":
                                TetradScore score = TetradScores.getInstance().getTetradScore(val);
                                if (score != null) {
                                    details.put("Score", score.getScore().getAnnotation().name());
                                }
                                break;
                            case "test":
                                TetradTest test = TetradTests.getInstance().getTetradTest(val);
                                if (test != null) {
                                    details.put("Test", test.getTest().getAnnotation().name());
                                }
                                break;
                        }
                    }
                });

        return details;
    }

    public Map<String, JobFile> getFiles(TetradJob tetradJob) {
        Map<String, JobFile> files = new LinkedHashMap<>();

        TetradDataFile tetradDataFile = tetradJob.getTetradDataFile();
        if (tetradDataFile != null) {
            Long id = tetradDataFile.getId();
            String name = tetradDataFile.getFile().getName();
            String varType = tetradDataFile.getVariableType().getName();

            int numOfCases = tetradDataFile.getNumOfCases();
            int numOfVars = tetradDataFile.getNumOfVars();
            String description = String.format("%d cases, %d variables (%s)", numOfCases, numOfVars, varType);

            files.put("Dataset", new JobFile(id, name, description, true));
        }

        FileGroup fileGroup = tetradJob.getFileGroup();
        if (fileGroup != null) {
            Long id = fileGroup.getId();
            String name = fileGroup.getName();
            String varType = fileGroup.getVariableType().getName();

            int numOfFiles = fileGroup.getFiles().size();
            String description = String.format("%d files (%s)", numOfFiles, varType);

            files.put("Dataset", new JobFile(id, name, description, false));
        }

        File knwlFile = tetradJob.getKnowledgeFile();
        if (knwlFile != null) {
            Long id = knwlFile.getId();
            String name = knwlFile.getName();

            files.put("Knowledge", new JobFile(id, name, null, true));
        }

        TetradVariableFile varFile = tetradJob.getTetradVariableFile();
        if (varFile != null) {
            Long id = varFile.getId();
            String name = varFile.getFile().getName();

            int numOfVars = varFile.getNumOfVars();
            String description = String.format("%d variables", numOfVars);

            files.put("Variable", new JobFile(id, name, description, true));
        }

        return files;
    }

    public Map<String, String> getJobSubmissionDetails(JobDetail jobDetail) {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("Created Date", DateFormatUtils.format(jobDetail.getCreationTime()));
        details.put("Start Date", DateFormatUtils.format(jobDetail.getStartTime()));
        details.put("End Date", DateFormatUtils.format(jobDetail.getEndTime()));
        details.put("Job Status", jobDetail.getJobStatus().getName());

        return details;
    }

    public JobDetail updateJobDetail(JobDetailForm jobDetailForm, JobDetail jobDetail) {
        jobDetail.setName(jobDetailForm.getName());
        jobDetail.setDescription(jobDetailForm.getDescription());
        try {
            jobDetail = jobDetailService.getRepository().save(jobDetail);
        } catch (Exception exception) {
            LOGGER.error("Unable to update job detail information.", exception);
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
