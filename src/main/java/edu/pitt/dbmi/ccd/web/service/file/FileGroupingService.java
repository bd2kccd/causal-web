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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.web.domain.file.FileGroupForm;
import java.util.Date;
import java.util.List;
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

    @Autowired
    public FileGroupingService(FileGroupService fileGroupService, FileService fileService, FileTypeService fileTypeService) {
        this.fileGroupService = fileGroupService;
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
    }

    public List<FileGroup> getFileGroups(UserAccount userAccount) {
        return fileGroupService.getRepository().findByUserAccount(userAccount);
    }

    public void addFileGroup(FileGroupForm fileGroupForm, UserAccount userAccount) {
        String name = fileGroupForm.getGroupName();
        List<Long> fileIds = fileGroupForm.getFileIds();

        List<File> files = fileService.getRepository().findByIdsAndUserAccount(fileIds, userAccount);
        if (!files.isEmpty()) {
            FileType fileType = fileTypeService.getRepository().findByName(FileTypeService.DATA);

            fileGroupService.getRepository().save(new FileGroup(name, new Date(System.currentTimeMillis()), fileType, userAccount, files));
        }
    }

    public boolean existed(FileGroupForm fileGroupForm, UserAccount userAccount) {
        return fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getGroupName(), userAccount);
    }

}
