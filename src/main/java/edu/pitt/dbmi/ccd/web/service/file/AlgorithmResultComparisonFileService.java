/*
 * Copyright (C) 2016 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.db.domain.FileTypeName;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 *
 * Aug 29, 2016 5:22:54 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgorithmResultComparisonFileService {

    private final UserAccountService userAccountService;
    private final FileManagementService fileManagementService;
    private final FileService fileService;

    @Autowired
    public AlgorithmResultComparisonFileService(UserAccountService userAccountService, FileManagementService fileManagementService, FileService fileService) {
        this.userAccountService = userAccountService;
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
    }

    public void listFiles(AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        FileType fileType = fileManagementService.findFileType(FileTypeName.ALGORITHM_RESULT_COMPARISON);
        fileManagementService.syncDatabaseWithDirectory(fileType, userAccount);

        model.addAttribute("pageTitle", "Algorithm Result Comparison Files");
        model.addAttribute("files", fileService.findByFileTypeAndUserAccount(fileType, userAccount));
    }

    public void showFileInfo(Long id, AppUser appUser, Model model) {
        fileManagementService.showFileInfo(id, appUser, model, false);
    }

}
