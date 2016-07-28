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
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CategorizeFile;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
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
        String url = getRedirect(fileType);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categorizeFile", bindingResult);
            redirectAttributes.addFlashAttribute("categorizeFile", categorizeFile);
            redirectAttributes.addFlashAttribute("collapse", Boolean.FALSE);

            return String.format("%s/%s?id=%d", url, INFO, id);
        }

        if (fileManagementService.categorizeFile(id, categorizeFile, appUser, redirectAttributes)) {
            return url;
        } else {
            return String.format("%s/%s?id=%d", url, INFO, id);
        }

    }

    private String getRedirect(FileType fileType) {
        String fileTypeName = (fileType == null) ? "" : fileType.getName();
        switch (fileTypeName) {
            case FileTypeService.ALGO_RESULT_TYPE_NAME:
                return REDIRECT_ALGO_RESULT_FILE;
            case FileTypeService.DATA_TYPE_NAME:
                return REDIRECT_DATA_INPUT;
            case FileTypeService.PRIOR_TYPE_NAME:
                return REDIRECT_PRIOR_KNOWLEDGE_INPUT;
            case FileTypeService.VAR_TYPE_NAME:
                return REDIRECT_VARIABLE_INPUT;
            default:
                return REDIRECT_NEW_UPLOAD;
        }
    }

}
