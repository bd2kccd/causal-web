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

import edu.pitt.dbmi.ccd.db.domain.FileTypeName;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CategorizeFile;
import edu.pitt.dbmi.ccd.web.domain.file.FileInfoUpdate;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping(value = "secured/file/management")
public class FileManagementController implements ViewPath {

    private final FileManagementService fileManagementService;

    @Autowired
    public FileManagementController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }

    @RequestMapping(value = "info/update", method = RequestMethod.POST)
    public String updateFileInfo(
            @Valid @ModelAttribute("fileInfoUpdate") final FileInfoUpdate fileInfoUpdate,
            final BindingResult bindingResult,
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes) {
        FileType fileType = fileManagementService.getFileType(id, appUser);
        String redirectUrl = String.format("%s/%s?id=%d", getRedirect(fileType), INFO, id);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.fileInfoUpdate", bindingResult);
            redirectAttributes.addFlashAttribute("fileInfoUpdate", fileInfoUpdate);
        } else {
            fileManagementService.updateFileInfo(id, fileInfoUpdate, appUser);
        }

        return redirectUrl;
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        fileManagementService.downloadFile(id, appUser, request, response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String delete(@RequestParam(value = "id") final Long id, @ModelAttribute("appUser") final AppUser appUser) {
        File file = fileManagementService.deleteFile(id, appUser);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        return getRedirect(file.getFileType());
    }

    @RequestMapping(value = CATEGORIZE, method = RequestMethod.POST)
    public String categorizeFile(
            @Valid @ModelAttribute("categorizeFile") final CategorizeFile categorizeFile,
            final BindingResult bindingResult,
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes) {
        FileType fileType = fileManagementService.getFileType(id, appUser);
        String redirectUrl = getRedirect(fileType);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categorizeFile", bindingResult);
            redirectAttributes.addFlashAttribute("categorizeFile", categorizeFile);
            redirectAttributes.addFlashAttribute("collapse", Boolean.FALSE);
            return String.format("%s/%s?id=%d", redirectUrl, INFO, id);
        }

        if (fileManagementService.categorizeFile(id, categorizeFile, appUser, redirectAttributes)) {
            return redirectUrl;
        } else {
            return String.format("%s/%s?id=%d", redirectUrl, INFO, id);
        }

    }

    private String getRedirect(FileType fileType) {
        if (fileType == null) {
            return REDIRECT_NEW_UPLOAD;
        }

        FileTypeName fileTypeName = FileTypeName.valueOf(fileType.getName());
        switch (fileTypeName) {
            case ALGORITHM_RESULT:
                return REDIRECT_RESULT_FILE;
            case ALGORITHM_RESULT_COMPARISON:
                return REDIRECT_RESULT_COMPARISON_FILE;
            case DATASET:
                return REDIRECT_DATASET_FILE;
            case PRIOR_KNOWLEDGE:
                return REDIRECT_PRIOR_KNOWLEDGE_FILE;
            case VARIABLE:
                return REDIRECT_DATA_VARIABLE_FILE;
            default:
                return REDIRECT_NEW_UPLOAD;
        }
    }

}
