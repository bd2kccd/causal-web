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
import edu.pitt.dbmi.causal.web.service.file.FileManagementService;
import edu.pitt.dbmi.ccd.db.code.FileFormatCodes;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping(value = "secured/ws/file")
public class FileRestController {

    private final AppUserService appUserService;
    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final FileGroupService fileGroupService;
    private final FileManagementService fileManagementService;

    @Autowired
    public FileRestController(AppUserService appUserService, FileService fileService, FileFormatService fileFormatService, FileGroupService fileGroupService, FileManagementService fileManagementService) {
        this.appUserService = appUserService;
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.fileGroupService = fileGroupService;
        this.fileManagementService = fileManagementService;
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteFile(@PathVariable final Long id, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        long count = fileGroupService.getRepository().countByFiles(Collections.singletonList(file));
        if (count > 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cannot delete file that is in a file group.  Remove the file from the group before first before deleting.");
        }

        if (fileManagementService.deleteFile(file, userAccount)) {
            return ResponseEntity.ok(id);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete file.");
        }
    }

    @GetMapping(value = "{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listUncategorizedFile(@PathVariable final short code, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        switch (code) {
            case -1:
                return ResponseEntity.ok(fileService.getAllFiles(userAccount));
            case 0:
                return ResponseEntity.ok(fileService.getRepository().getUncategorizedFiles(userAccount));
            case FileFormatCodes.TETRAD_KNWL:
            case FileFormatCodes.TETRAD_TAB:
            case FileFormatCodes.TETRAD_VAR:
                FileFormat fileFormat = fileFormatService.findByCode(code);
                if (fileFormat == null) {
                    return ResponseEntity.notFound().build();
                } else {
                    return ResponseEntity.ok(fileService.getRepository().getByFileFormat(fileFormat, userAccount));
                }
            default:
                return ResponseEntity.notFound().build();
        }
    }

}
