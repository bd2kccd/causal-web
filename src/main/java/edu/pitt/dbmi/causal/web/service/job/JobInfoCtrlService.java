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

import edu.cmu.tetrad.util.ParamDescriptions;
import edu.pitt.dbmi.causal.web.service.filesys.FileManagementService;
import edu.pitt.dbmi.causal.web.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.web.tetrad.TetradScores;
import edu.pitt.dbmi.causal.web.tetrad.TetradTests;
import edu.pitt.dbmi.causal.web.util.DateFormatUtils;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.JobInfo;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Apr 13, 2018 6:15:48 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class JobInfoCtrlService {

    private final Pattern PIPE_PATTERN = Pattern.compile("\\|");
    private final Pattern COLON_PATTERN = Pattern.compile(":");

    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final FileManagementService fileManagementService;

    @Autowired
    public JobInfoCtrlService(FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, FileManagementService fileManagementService) {
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileManagementService = fileManagementService;
    }

    public List<String> listResultFiles(JobInfo jobInfo, UserAccount userAccount) {
        return fileManagementService.listResultFiles(jobInfo.getName(), userAccount);
    }

    public Map<String, String> getAlgoInfos(Map<String, String> parameters) {
        TetradAlgorithms algo = TetradAlgorithms.getInstance();
        TetradScores scores = TetradScores.getInstance();
        TetradTests test = TetradTests.getInstance();

        Map<String, String> infos = new LinkedHashMap<>();
        parameters.entrySet().stream()
                .forEach(e -> {
                    switch (e.getKey()) {
                        case "algorithm":
                            infos.put(StringUtils.capitalize(e.getKey()), algo.getTetradAlgorithm(e.getValue()).getAlgorithm().getAnnotation().name());
                            break;
                        case "score":
                            infos.put(StringUtils.capitalize(e.getKey()), scores.getTetradScore(e.getValue()).getScore().getAnnotation().name());
                            break;
                        case "test":
                            infos.put(StringUtils.capitalize(e.getKey()), test.getTetradTest(e.getValue()).getTest().getAnnotation().name());
                            break;
                    }
                });

        return infos;
    }

    public Map<String, String> getAlgoParameters(Map<String, String> parameters) {
        ParamDescriptions paramDescs = ParamDescriptions.getInstance();

        return parameters.entrySet().stream()
                .filter(e -> !e.getKey().equals("algorithm"))
                .filter(e -> !e.getKey().equals("score"))
                .filter(e -> !e.getKey().equals("test"))
                .collect(Collectors.toMap(e -> paramDescs.get(e.getKey()).getDescription(), Map.Entry::getValue));
    }

    public Map<String, String> getGeneralInfo(JobInfo jobInfo) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Name", jobInfo.getName());
        info.put("Created Date", DateFormatUtils.format(jobInfo.getCreationTime()));
        info.put("Start Date", DateFormatUtils.format(jobInfo.getStartTime()));
        info.put("End Date", DateFormatUtils.format(jobInfo.getEndTime()));
        info.put("Algorithm", jobInfo.getAlgorithmType().getName());
        info.put("Location", jobInfo.getJobLocation().getName());
        info.put("Status", jobInfo.getJobStatus().getName());

        return info;
    }

    public List<File> getFiles(Long datasetId, boolean isSingleDataset) {
        if (isSingleDataset) {
            Optional<TetradDataFile> opt = tetradDataFileService.getRepository().findById(datasetId);
            if (opt.isPresent()) {
                return Collections.singletonList(opt.get().getFile());
            }
        } else {
            Optional<FileGroup> opt = fileGroupService.getRepository().findById(datasetId);
            if (opt.isPresent()) {
                return opt.get().getFiles();
            }
        }

        return Collections.EMPTY_LIST;
    }

    public Map<String, String> parseParameters(String strParams) {
        Map<String, String> params = new TreeMap<>();

        Arrays.stream(PIPE_PATTERN.split(strParams))
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

}
