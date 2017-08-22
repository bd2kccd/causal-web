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
package edu.pitt.dbmi.causal.web.service.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 30, 2017 5:00:21 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileGroupCtrlService {

    private final FileGroupService fileGroupService;
    private final FileTypeService fileTypeService;
    private final TetradDataFileService tetradDataFileService;

    @Autowired
    public FileGroupCtrlService(FileGroupService fileGroupService, FileTypeService fileTypeService, TetradDataFileService tetradDataFileService) {
        this.fileGroupService = fileGroupService;
        this.fileTypeService = fileTypeService;
        this.tetradDataFileService = tetradDataFileService;
    }

    public void updateFileGroup(String fileGroupName, List<TetradDataFile> tetradDataFiles, FileGroup fileGroup) {
        List<File> files = tetradDataFiles.stream()
                .map(TetradDataFile::getFile)
                .collect(Collectors.toList());

        // update file group
        fileGroup.setName(fileGroupName);
        fileGroup.setFiles(files);

        fileGroupService.getRepository().save(fileGroup);
    }

    public void addNewFileGroup(String fileGroupName, List<TetradDataFile> tetradDataFiles, UserAccount userAccount) {
        FileType fileType = fileTypeService.findByName(FileTypeService.DATA_NAME);
        List<File> files = tetradDataFiles.stream()
                .map(TetradDataFile::getFile)
                .collect(Collectors.toList());

        fileGroupService.getRepository().save(new FileGroup(fileGroupName, new Date(System.currentTimeMillis()), fileType, userAccount, files));
    }

    public Map<Long, List<File>> getTetradGroupedData(List<FileVariableType> fileVariableTypes, UserAccount userAccount) {
        Map<Long, List<File>> mapFiles = new HashMap<>();

        // initialize map files
        fileVariableTypes.forEach(varType -> {
            mapFiles.put(varType.getId(), new LinkedList<>());
        });

        List<TetradDataFile> tetradDataFiles = tetradDataFileService.getRepository().findByUserAccount(userAccount);
        tetradDataFiles.forEach(data -> {
            FileVariableType varType = data.getFileVariableType();
            mapFiles.get(varType.getId()).add(data.getFile());
        });

        return mapFiles;
    }

    public Map<String, List<File>> getTetradDataFileGroupByVariableType(List<FileVariableType> fileVariableTypes, UserAccount userAccount) {
        Map<String, List<File>> mapFiles = new HashMap<>();

        // initialize map files
        fileVariableTypes.forEach(varType -> {
            mapFiles.put(varType.getName(), new LinkedList<>());
        });

        List<TetradDataFile> tetradDataFiles = tetradDataFileService.getRepository().findByUserAccount(userAccount);
        tetradDataFiles.forEach(data -> {
            FileVariableType varType = data.getFileVariableType();
            mapFiles.get(varType.getName()).add(data.getFile());
        });

        return mapFiles;
    }

}
