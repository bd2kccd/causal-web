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
import edu.pitt.dbmi.ccd.web.domain.file.FileInfoUpdate;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    private final FileManagementService fileManagementService;
    private final AppUserService appUserService;

    public FileManagementController(FileManagementService fileManagementService, AppUserService appUserService) {
        this.fileManagementService = fileManagementService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = "info/update", method = RequestMethod.POST)
    public String updateFileInfo(
            @Valid @ModelAttribute("fileInfoUpdate") final FileInfoUpdate fileInfoUpdate,
            final BindingResult bindingResult,
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest req) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileManagementService.retrieveFile(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        String redirectUrl = getRedirect(req, file.getFileType()) + "?id=" + file.getId();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fileInfoUpdate", bindingResult);
            redirectAttributes.addFlashAttribute("fileInfoUpdate", fileInfoUpdate);
        } else {
            fileManagementService.updateFileInfo(file, fileInfoUpdate);
        }

        return redirectUrl;
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

        if (!model.containsAttribute("fileInfoUpdate")) {
            model.addAttribute("fileInfoUpdate", new FileInfoUpdate(file.getTitle()));
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

        fileManagementService.deleteFile(file, userAccount);

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
