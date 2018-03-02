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
package edu.pitt.dbmi.causal.web.ctrl.file;

import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.filesys.FileManagementService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Feb 20, 2018 3:23:25 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file")
public class FileRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileRestController.class);

    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final FileService fileService;
    private final FileFormatService fileFormatService;

    @Autowired
    public FileRestController(AppUserService appUserService, FileManagementService fileManagementService, FileService fileService, FileFormatService fileFormatService) {
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteFile(@PathVariable final Long id, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        if (fileManagementService.deleteFile(file, userAccount)) {
            return ResponseEntity.ok(id);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete file.");
        }
    }

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

        if (!title.equals(file.getTitle())) {
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

    @RequestMapping(value = "format/uncategorized", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listUncategorizedFile(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        fileManagementService.syncFilesWithDatabase(userAccount);

        return ResponseEntity.ok(fileService.getRepository().findByUserAccountAndFileFormatIsNull(userAccount));
    }

    @RequestMapping(value = "format/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFilesByCategory(@PathVariable final Long id, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        FileFormat fileFormat = fileFormatService.findById(id);
        if (fileFormat == null) {
            return ResponseEntity.notFound().build();
        }

        fileManagementService.syncFilesWithDatabase(userAccount);

        return ResponseEntity.ok(fileService.getRepository().findByUserAccountAndFileFormat(userAccount, fileFormat));
    }

}
