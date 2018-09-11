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

import edu.pitt.dbmi.causal.web.util.DateFormatUtils;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.JobDetail;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.TetradVariableFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 25, 2018 1:34:16 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class JobInfoService {

    private final Pattern PIPE_PATTERN = Pattern.compile("\\|");
    private final Pattern COLON_PATTERN = Pattern.compile(":");

    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;

    @Autowired
    public JobInfoService(FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService) {
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
    }

    public List<File> getVariableFiles(Map<String, String> params) {
        String varFile = params.get("varFile");
        if (varFile != null) {
            Optional<TetradVariableFile> opt = tetradVariableFileService.getRepository()
                    .findById(Long.parseLong(varFile));
            if (opt.isPresent()) {
                return Collections.singletonList(opt.get().getFile());
            }
        }

        return Collections.EMPTY_LIST;
    }

    public List<TetradDataFile> getTetradDataFiles(Map<String, String> params, UserAccount userAccount) {
        String singleDataFile = params.get("singleDataFile");
        String dataFile = params.get("dataFile");

        boolean isSingleDataFile = singleDataFile != null && singleDataFile.equals("yes");
        Long dataFileId = Long.parseLong(dataFile);

        if (isSingleDataFile) {
            TetradDataFile tetradDataFile = tetradDataFileService.getRepository()
                    .findByIdAndUserAccount(dataFileId, userAccount);
            if (tetradDataFile != null) {
                return Collections.singletonList(tetradDataFile);
            }
        } else {
            FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(dataFileId, userAccount);
            if (fileGroup != null) {
                return tetradDataFileService.getRepository()
                        .findByUserAccountAndFiles(userAccount, fileGroup.getFiles());
            }
        }

        return Collections.EMPTY_LIST;
    }

    public List<File> getDataFiles(Map<String, String> params) {
        String singleDataFile = params.get("singleDataFile");
        String dataFile = params.get("dataFile");

        boolean isSingleDataFile = singleDataFile != null && singleDataFile.equals("yes");
        Long dataFileId = Long.parseLong(dataFile);

        if (isSingleDataFile) {
            Optional<TetradDataFile> opt = tetradDataFileService.getRepository().findById(dataFileId);
            if (opt.isPresent()) {
                return Collections.singletonList(opt.get().getFile());
            }
        } else {
            Optional<FileGroup> opt = fileGroupService.getRepository().findById(dataFileId);
            if (opt.isPresent()) {
                return opt.get().getFiles();
            }
        }

        return Collections.EMPTY_LIST;
    }

    public Map<String, String> getGeneralDetails(JobDetail jobDetail) {
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
        Map<String, String> params = new TreeMap<>();

//        Arrays.stream(PIPE_PATTERN.split(jobDetail.getJobParameter()))
//                .forEach(e -> {
//                    String[] keyVal = COLON_PATTERN.split(e);
//                    if (keyVal.length == 2) {
//                        String key = keyVal[0];
//                        String val = keyVal[1];
//                        params.put(key, (val.equals("true") ? "yes" : val.equals("false") ? "no" : val));
//                    }
//                });
        return params;
    }

}
