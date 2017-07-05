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
package edu.pitt.dbmi.ccd.web.service.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.web.domain.file.FileGroupForm;
import java.util.Date;
import java.util.List;
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
public class FileGroupingService {

    private final FileGroupService fileGroupService;
    private final FileService fileService;
    private final FileTypeService fileTypeService;
    private final FileVariableTypeService fileVariableTypeService;
    private final TetradDataFileService tetradDataFileService;

    @Autowired
    public FileGroupingService(FileGroupService fileGroupService, FileService fileService, FileTypeService fileTypeService, FileVariableTypeService fileVariableTypeService, TetradDataFileService tetradDataFileService) {
        this.fileGroupService = fileGroupService;
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.tetradDataFileService = tetradDataFileService;
    }

    public List<FileGroup> getFileGroups(UserAccount userAccount) {
        return fileGroupService.getRepository().findByUserAccount(userAccount);
    }

    public void addFileGroup(FileGroupForm fileGroupForm, UserAccount userAccount) {
        String name = fileGroupForm.getGroupName();
        Long fileVariableTypeId = fileGroupForm.getFileVariableTypeId();
        List<Long> fileIds = fileGroupForm.getFileIds();

        FileVariableType fileVariableType = fileVariableTypeService.getRepository().findOne(fileVariableTypeId);
        if (fileVariableType != null) {
            List<TetradDataFile> dataFiles = tetradDataFileService.getRepository()
                    .findByFileVariableTypeAndAndFileIdsAndUserAccount(fileVariableType, fileIds, userAccount);
            if (!dataFiles.isEmpty()) {
                FileType fileType = fileTypeService.getRepository().findByName(FileTypeService.DATA);
                List<File> files = dataFiles.stream()
                        .map(TetradDataFile::getFile)
                        .collect(Collectors.toList());

                fileGroupService.getRepository().save(new FileGroup(name, new Date(System.currentTimeMillis()), fileType, userAccount, files));
            }
        }
    }

}
