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
package edu.pitt.dbmi.causal.web.service.algorithm;

import edu.pitt.dbmi.causal.web.prop.TetradProperties;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 27, 2017 3:02:16 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class TetradJobService {

    private final File EMPTY_FILE = new File("", "none", null, 0, null);

    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final TetradDataFileService tetradDataFileService;
    private final FileVariableTypeService fileVariableTypeService;
    private final TetradProperties tetradProperties;

    @Autowired
    public TetradJobService(FileService fileService, FileFormatService fileFormatService, TetradDataFileService tetradDataFileService, FileVariableTypeService fileVariableTypeService, TetradProperties tetradProperties) {
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.tetradProperties = tetradProperties;
    }

    public String getAlgorithmTitle(String algorithm) {
        return tetradProperties.getAlgoTitles().get(algorithm);
    }

    public List<File> getVariableOpts(UserAccount userAccount) {
        List<File> files = new LinkedList<>();
        files.add(EMPTY_FILE);

        FileFormat variableFileFormat = fileFormatService.findByName(FileFormatService.TETRAD_VARIABLE_NAME);
        List<File> variableFiles = fileService.getRepository().findByUserAccountAndFileFormat(userAccount, variableFileFormat);
        files.addAll(variableFiles);

        return files;
    }

    public List<File> getKnowledgeOpts(UserAccount userAccount) {
        List<File> files = new LinkedList<>();
        files.add(EMPTY_FILE);

        FileFormat knowledgeFileFormat = fileFormatService.findByName(FileFormatService.TETRAD_KNOWLEDGE_NAME);
        List<File> knowledgeFiles = fileService.getRepository().findByUserAccountAndFileFormat(userAccount, knowledgeFileFormat);
        files.addAll(knowledgeFiles);

        return files;
    }

    public List<File> getDatasetOpts(String fileVariableTypeName, UserAccount userAccount) {
        FileVariableType fileVariableType = fileVariableTypeService.findByName(fileVariableTypeName);
        List<TetradDataFile> dataFiles = tetradDataFileService.getRepository().findByFileVariableTypeAndAndUserAccount(fileVariableType, userAccount);

        return dataFiles.stream().map(TetradDataFile::getFile).collect(Collectors.toList());
    }

}
