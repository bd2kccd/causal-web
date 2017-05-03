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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.file.UncategorizedFileService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * May 3, 2017 2:25:20 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "secured/rest/file/uncategorized")
public class UncategorizedFileRestController {

    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final UncategorizedFileService uncategorizedFileService;

    @Autowired
    public UncategorizedFileRestController(AppUserService appUserService, FileManagementService fileManagementService, UncategorizedFileService uncategorizedFileService) {
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.uncategorizedFileService = uncategorizedFileService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<File> showFiles(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        return uncategorizedFileService.getUncategorizedFiles(userAccount);
    }

}
