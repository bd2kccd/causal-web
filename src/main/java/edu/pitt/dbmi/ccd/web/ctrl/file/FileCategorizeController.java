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
import edu.pitt.dbmi.ccd.db.service.FileDelimiterTypeService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.FileCategorizeForm;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.file.FileCategorizeCtrlService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jul 10, 2017 10:37:19 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/categorize")
public class FileCategorizeController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCategorizeController.class);

    private final FileService fileService;
    private final FileTypeService fileTypeService;
    private final FileDelimiterTypeService fileDelimiterTypeService;
    private final FileVariableTypeService fileVariableTypeService;
    private final AppUserService appUserService;
    private final FileCategorizeCtrlService fileCategorizeCtrlService;

    @Autowired
    public FileCategorizeController(FileService fileService,
            FileTypeService fileTypeService,
            FileDelimiterTypeService fileDelimiterTypeService,
            FileVariableTypeService fileVariableTypeService,
            AppUserService appUserService,
            FileCategorizeCtrlService fileCategorizeCtrlService) {
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileDelimiterTypeService = fileDelimiterTypeService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.appUserService = appUserService;
        this.fileCategorizeCtrlService = fileCategorizeCtrlService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String categorizeFile(
            @Valid @ModelAttribute("fileCategorizeForm") final FileCategorizeForm fileCategorizeForm,
            final BindingResult bindingResult,
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileCategorizeForm", bindingResult);
            redirAttrs.addFlashAttribute("fileCategorizeForm", fileCategorizeForm);

            return REDIRECT_FILE_CATEGORIZE + id;
        }
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        file = fileCategorizeCtrlService.categorizeFile(fileCategorizeForm, file, userAccount);

        return getRedirect(file);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFileCategorize(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("file", file);
        model.addAttribute("addInfo", fileCategorizeCtrlService.getAdditionalInfo(file));
        model.addAttribute("collapse", file.getFileFormat() != null);
        model.addAttribute("fileTypes", fileTypeService.findAll());
        model.addAttribute("fileFormatGroups", fileCategorizeCtrlService.getFileFormatGroupOpts());
        model.addAttribute("fileDelimiterTypes", fileDelimiterTypeService.findAll());
        model.addAttribute("fileVariableTypes", fileVariableTypeService.findAll());
        model.addAttribute("tetradDataGroupId", fileCategorizeCtrlService.getTetradDataGroupId());

        if (!model.containsAttribute("fileCategorizeForm")) {
            model.addAttribute("fileCategorizeForm", fileCategorizeCtrlService.toFileCategorizeForm(file));
        }

        return FILE_CATEGORIZE_VIEW;
    }

    private String getRedirect(File file) {
        FileFormat fileFmt = file.getFileFormat();
        String fileFmtName = (fileFmt == null) ? "uncategorized" : fileFmt.getName();

        return String.format("redirect:/secured/file/%s", fileFmtName);
    }

}
