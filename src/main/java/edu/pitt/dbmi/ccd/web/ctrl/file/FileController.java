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
package edu.pitt.dbmi.ccd.web.ctrl.file;

import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.file.FileCtrlService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jun 26, 2017 5:14:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file")
public class FileController implements ViewPath {

    private final FileService fileService;
    private final FileManagementService fileManagementService;
    private final FileCtrlService fileCtrlService;
    private final FileFormatService fileFormatService;
    private final AppUserService appUserService;

    @Autowired
    public FileController(FileService fileService, FileManagementService fileManagementService, FileCtrlService fileCtrlService, FileFormatService fileFormatService, AppUserService appUserService) {
        this.fileService = fileService;
        this.fileManagementService = fileManagementService;
        this.fileCtrlService = fileCtrlService;
        this.fileFormatService = fileFormatService;
        this.appUserService = appUserService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String categorizeFile(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("fileCategoryPanels", fileCtrlService.getFileCategoryPanels(userAccount));

        return FILE_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list/{fileFormatName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFiles(final @PathVariable String fileFormatName, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        if ("uncategorized".equals(fileFormatName)) {
            return ResponseEntity.ok(fileService.getRepository().findByUserAccountAndFileFormatIsNull(userAccount));
        } else {
            FileFormat fileFormat = fileFormatService.getRepository().findByName(fileFormatName);

            return (fileFormat == null)
                    ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(fileService.getRepository().findByUserAccountAndFileFormat(userAccount, fileFormat));
        }
    }

    @RequestMapping(value = "{fileFormatName}", method = RequestMethod.GET)
    public String showFiles(@PathVariable String fileFormatName, final Model model) {
        FileFormat fileFormat = fileFormatService.getRepository().findByName(fileFormatName);
        if (fileFormat == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("fileListView", fileCtrlService.getFileListView(fileFormatName));

        return FILE_LIST_VIEW;
    }

}
