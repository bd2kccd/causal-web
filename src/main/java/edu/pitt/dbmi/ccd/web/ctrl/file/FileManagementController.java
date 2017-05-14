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
package edu.pitt.dbmi.ccd.web.ctrl.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 24, 2016 7:38:41 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/mgmt")
public class FileManagementController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagementController.class);

    private final FileManagementService fileManagementService;
    private final AppUserService appUserService;

    public FileManagementController(FileManagementService fileManagementService, AppUserService appUserService) {
        this.fileManagementService = fileManagementService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ResponseBody
    @RequestMapping(value = "title", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFiles(
            @RequestParam(value = "pk") final Long id,
            @RequestParam(value = "value") final String title,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileManagementService.retrieveFile(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required.");
        } else {
            if (file.getTitle().compareTo(title) != 0) {
                if (fileManagementService.existTitle(title, userAccount)) {
                    return ResponseEntity.badRequest().body("Title already in used. Plese enter a different title.");
                } else {
                    try {
                        fileManagementService.updateFileTitle(file, title);
                    } catch (Exception exception) {
                        LOGGER.error(exception.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update file title.");
                    }
                }
            }
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "categorize", method = RequestMethod.GET)
    public String categorizeFile(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileManagementService.retrieveFile(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("file", file);

        return CATEGORIZED_FILE_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResponseEntity<?> listFiles(
            @RequestParam(value = "id") final Long id,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileManagementService.retrieveFile(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            fileManagementService.deleteFile(file, userAccount);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete file.");
        }

        return ResponseEntity.ok(file.getId());
    }

    private String getRedirect(HttpServletRequest req, FileType fileType) {
        String referer = req.getHeader("referer");
        if (referer == null || referer.isEmpty()) {
            if (fileType == null) {
                return REDIRECT_UNCATEGORIZED_FILE;
            }

            switch (fileType.getName()) {
                default:
                    return REDIRECT_UNCATEGORIZED_FILE;
            }
        } else {
            try {
                URL url = new URL(referer);

                return "redirect:" + url.getPath().replaceFirst("/ccd", "");
            } catch (MalformedURLException exception) {
                return REDIRECT_UNCATEGORIZED_FILE;
            }
        }
    }

}
