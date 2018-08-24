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

import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.file.FileGroupForm;
import edu.pitt.dbmi.ccd.db.code.VariableTypeCodes;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
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

    private final FileGroupService fileGroupService;
    private final VariableTypeService variableTypeService;
    private final TetradDataFileService tetradDataFileService;

    @Autowired
    public GroupFileService(FileGroupService fileGroupService, VariableTypeService variableTypeService, TetradDataFileService tetradDataFileService) {
        this.fileGroupService = fileGroupService;
        this.variableTypeService = variableTypeService;
        this.tetradDataFileService = tetradDataFileService;
    }

    public FileGroup saveFileGroup(FileGroupForm fileGroupForm, FileGroup fileGroup, UserAccount userAccount) throws ResourceNotFoundException {
        VariableType variableType = variableTypeService.findById(fileGroupForm.getVarTypeId());
        if (variableType == null) {
            throw new ResourceNotFoundException("No such variable type found.");
        }

        List<File> files = tetradDataFileService.getRepository()
                .find(userAccount, variableType, fileGroupForm.getFileIds()).stream()
                .map(TetradDataFile::getFile)
                .collect(Collectors.toList());
        if (files.isEmpty()) {
            throw new ResourceNotFoundException("No such dataset found.");
        }

        fileGroup.setName(fileGroupForm.getName());
        fileGroup.setDescription(fileGroupForm.getDescription());
        fileGroup.setVariableType(variableType);
        fileGroup.setFiles(files);

        return fileGroupService.getRepository().save(fileGroup);
    }

    public FileGroup addNewFileGroup(FileGroupForm fileGroupForm, UserAccount userAccount) throws ResourceNotFoundException {
        VariableType variableType = variableTypeService.findById(fileGroupForm.getVarTypeId());
        if (variableType == null) {
            throw new ResourceNotFoundException("No such variable type found.");
        }

        List<File> files = tetradDataFileService.getRepository()
                .find(userAccount, variableType, fileGroupForm.getFileIds()).stream()
                .map(TetradDataFile::getFile)
                .collect(Collectors.toList());
        if (files.isEmpty()) {
            throw new ResourceNotFoundException("No such dataset found.");
        }

        FileGroup fileGroup = new FileGroup();
        fileGroup.setName(fileGroupForm.getName());
        fileGroup.setDescription(fileGroupForm.getDescription());
        fileGroup.setCreationTime(new Date());
        fileGroup.setVariableType(variableType);
        fileGroup.setFiles(files);
        fileGroup.setUserAccount(userAccount);

        return fileGroupService.getRepository().save(fileGroup);
    }

    public FileGroupForm createFileGroupForm() {
        VariableType variableType = variableTypeService
                .findByCode(VariableTypeCodes.CONTINUOUS);

        FileGroupForm fileGroupForm = new FileGroupForm();
        fileGroupForm.setVarTypeId(variableType.getId());

        return fileGroupForm;
    }

    public FileGroupForm createFileGroupForm(FileGroup fileGroup) {
        String name = fileGroup.getName();
        String description = fileGroup.getDescription();
        Long varTypeId = fileGroup.getVariableType().getId();

        List<Long> fileIds = fileGroup.getFiles().stream()
                .map(File::getId)
                .collect(Collectors.toList());

        FileGroupForm form = new FileGroupForm();
        form.setName(name);
        form.setDescription(description);
        form.setVarTypeId(varTypeId);
        form.setFileIds(fileIds);

        return form;
    }

}
