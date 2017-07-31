/*
 * Copyright (C) 2017 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.web.model.algorithm.AlgorithmItem;
import edu.pitt.dbmi.ccd.web.model.algorithm.TetradJobForm;
import edu.pitt.dbmi.ccd.web.model.algorithm.TetradJobPage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 21, 2017 12:03:58 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class TetradAlgoFormService {

    private final File EMPTY_FILE = new File("", "none", null, 0, null);

    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final TetradDataFileService tetradDataFileService;
    private final FileVariableTypeService fileVariableTypeService;
    private final Map<String, AlgorithmItem> algorithmItems;

    @Autowired
    public TetradAlgoFormService(FileService fileService, FileFormatService fileFormatService, TetradDataFileService tetradDataFileService, FileVariableTypeService fileVariableTypeService, Map<String, AlgorithmItem> algorithmItems) {
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.algorithmItems = algorithmItems;
    }

    public TetradJobForm populateForm(TetradJobForm tetradJobForm, TetradJobPage jobPage) {
        List<File> files = jobPage.getDatasetOpts();
        if (!files.isEmpty()) {
            tetradJobForm.setDataset(files.get(0).getTitle());
        }
        files = jobPage.getKnowledgeOpts();
        if (!files.isEmpty()) {
            tetradJobForm.setKnowledge(files.get(0).getTitle());
        }

        return tetradJobForm;
    }

    public TetradJobPage createJobSubmissionPage(String algorithm, UserAccount userAccount) {
        AlgorithmItem algorithmItem = algorithmItems.get(algorithm);

        TetradJobPage page = new TetradJobPage();
        page.setTitle((algorithmItem == null) ? "" : algorithmItem.getTitle());
        page.setDatasetOpts(getDatasetFiles(userAccount));
        page.setKnowledgeOpts(getKnowledgeFiles(userAccount));

        return page;
    }

    private List<File> getKnowledgeFiles(UserAccount userAccount) {
        List<File> files = new LinkedList<>();

        files.add(EMPTY_FILE);

        FileFormat knowledgeFileFormat = fileFormatService.findByName(FileFormatService.TETRAD_KNOWLEDGE_NAME);
        List<File> knowledgeFiles = fileService.getRepository().findByUserAccountAndFileFormat(userAccount, knowledgeFileFormat);
        files.addAll(knowledgeFiles);

        return files;
    }

    private List<File> getDatasetFiles(UserAccount userAccount) {
        FileVariableType fileVariableType = fileVariableTypeService.findByName(FileVariableTypeService.CONTINUOUS_NAME);
        List<TetradDataFile> dataFiles = tetradDataFileService.getRepository().findByFileVariableTypeAndAndUserAccount(fileVariableType, userAccount);

        return dataFiles.stream().map(TetradDataFile::getFile).collect(Collectors.toList());
    }

}
