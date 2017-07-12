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

import edu.pitt.dbmi.ccd.db.entity.File;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final FileCtrlService fileCtrlService;
    private final FileFormatService fileFormatService;
    private final FileManagementService fileManagementService;
    private final AppUserService appUserService;

    @Autowired
    public FileController(FileService fileService, FileCtrlService fileCtrlService, FileFormatService fileFormatService, FileManagementService fileManagementService, AppUserService appUserService) {
        this.fileService = fileService;
        this.fileCtrlService = fileCtrlService;
        this.fileFormatService = fileFormatService;
        this.fileManagementService = fileManagementService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ResponseBody
    @RequestMapping(value = "title", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateFileTitle(
            @RequestParam(value = "pk", required = true) final Long id,
            @RequestParam(value = "value", required = true) final String title,
            final AppUser appUser) {
        if (id == null) {
            return ResponseEntity.badRequest().body("File ID is required.");
        }
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required.");
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        if (!title.equals(file)) {
            if (fileService.getRepository().existsByTitleAndUserAccount(title, userAccount)) {
                return ResponseEntity.badRequest().body("Title already in used. Plese enter a different title.");
            } else {
                file.setTitle(title);
                try {
                    fileService.getRepository().save(file);
                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update file title.");
                }
            }
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFiles(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        fileManagementService.syncDatabaseWithDataDirectory(userAccount);

        model.addAttribute("fileSummaryGroups", fileCtrlService.getFileSummaryGroups(userAccount));

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
            FileFormat fileFormat = fileFormatService.findByName(fileFormatName);

            return (fileFormat == null)
                    ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(fileService.getRepository().findByUserAccountAndFileFormat(userAccount, fileFormat));
        }
    }

    @RequestMapping(value = "{fileFormatName}", method = RequestMethod.GET)
    public String showFileList(@PathVariable String fileFormatName, final Model model) {
        if ("uncategorized".equals(fileFormatName) || fileFormatService.findByName(fileFormatName) != null) {
            model.addAttribute("fileListView", fileCtrlService.getFileListView(fileFormatName));
        } else {
            throw new ResourceNotFoundException();
        }

        return FILE_LIST_VIEW;
    }

}
