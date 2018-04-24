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
package edu.pitt.dbmi.causal.web.service.file;

import edu.pitt.dbmi.causal.web.model.file.FileGroupForm;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Mar 1, 2018 1:18:12 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class GroupFileService {

    private final VariableTypeService variableTypeService;
    private final FileGroupService fileGroupService;
    private final FileTypeService fileTypeService;
    private final TetradDataFileService tetradDataFileService;

    @Autowired
    public GroupFileService(VariableTypeService variableTypeService, FileGroupService fileGroupService, FileTypeService fileTypeService, TetradDataFileService tetradDataFileService) {
        this.variableTypeService = variableTypeService;
        this.fileGroupService = fileGroupService;
        this.fileTypeService = fileTypeService;
        this.tetradDataFileService = tetradDataFileService;
    }

    public FileGroup updateFileGroup(FileGroup fileGroup, String name, List<File> files) {
        fileGroup.setName(name);
        fileGroup.setFiles(files);

        return fileGroupService.getRepository().save(fileGroup);
    }

    public FileGroup saveFileGroup(String name, VariableType varType, UserAccount userAccount, List<File> files) {
        return fileGroupService.getRepository()
                .save(new FileGroup(name, new Date(), varType, userAccount, files));
    }

    public FileGroupForm createFileGroupForm() {
        FileGroupForm fileGroupForm = new FileGroupForm();

        List<VariableType> varTypes = variableTypeService.findAll();
        if (!varTypes.isEmpty()) {
            fileGroupForm.setVarTypeId(varTypes.get(0).getId());
        }

        return fileGroupForm;
    }

    public FileGroupForm createFileGroupForm(FileGroup fileGroup) {
        String groupName = fileGroup.getName();
        Long varTypeId = fileGroup.getVariableType().getId();

        List<Long> fileIds = fileGroup.getFiles().stream()
                .map(File::getId)
                .collect(Collectors.toList());

        return new FileGroupForm(groupName, varTypeId, fileIds);
    }

}
