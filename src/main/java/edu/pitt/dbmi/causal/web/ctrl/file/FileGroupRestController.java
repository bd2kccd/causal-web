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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Feb 28, 2018 2:21:01 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/ws/file/group")
public class FileGroupRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileGroupRestController.class);

    private final AppUserService appUserService;
    private final FileGroupService fileGroupService;

    @Autowired
    public FileGroupRestController(AppUserService appUserService, FileGroupService fileGroupService) {
        this.appUserService = appUserService;
        this.fileGroupService = fileGroupService;
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteFile(@PathVariable final Long id, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        if (fileGroupService.getRepository().existsByIdAndUserAccount(id, userAccount)) {
            try {
                fileGroupService.getRepository()
                        .deleteByIdAndUserAccount(id, userAccount);
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unable to delete file group.");
            }

            return ResponseEntity.ok(id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
