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

import edu.pitt.dbmi.causal.web.ctrl.SitePaths;
import edu.pitt.dbmi.causal.web.ctrl.SiteViews;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.file.FileDetailForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.FileCategorizationService;
import edu.pitt.dbmi.causal.web.service.file.FileDetailService;
import edu.pitt.dbmi.causal.web.service.file.FileManagementService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 26, 2017 5:14:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file")
public class FileController {

    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final FileFormatService fileFormatService;
    private final FileService fileService;
    private final FileDetailService fileDetailService;
    private final FileCategorizationService fileCategorizationService;

    @Autowired
    public FileController(AppUserService appUserService, FileManagementService fileManagementService, FileFormatService fileFormatService, FileService fileService, FileDetailService fileDetailService, FileCategorizationService fileCategorizationService) {
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.fileFormatService = fileFormatService;
        this.fileService = fileService;
        this.fileDetailService = fileDetailService;
        this.fileCategorizationService = fileCategorizationService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping("{id}")
    public String updateFileDetails(
            @Valid @ModelAttribute("fileDetailForm") FileDetailForm fileDetailForm,
            final BindingResult bindingResult,
            @PathVariable final Long id,
            final Model model,
            final AppUser appUser,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileDetailForm", bindingResult);
            redirAttrs.addFlashAttribute("fileDetailForm", fileDetailForm);
            redirAttrs.addFlashAttribute("errorMsg", Boolean.TRUE);

            return SitePaths.REDIRECT_FILE + "/" + id;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        fileDetailService.updateDetails(fileDetailForm, file);

        return SitePaths.REDIRECT_FILE + "/" + id;
    }

    @GetMapping("{id}")
    public String showFileDetails(@PathVariable final Long id, final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("file", file);
        model.addAttribute("categorizationDetails", fileDetailService.getCategorizationDetails(file));
        if (!model.containsAttribute("fileDetailForm")) {
            model.addAttribute("fileDetailForm", fileDetailService.getFileDetailForm(file));
        }

        return SiteViews.FILE_DETAIL;
    }

    @GetMapping
    public String listFiles(final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        fileManagementService.syncFilesWithDatabase(userAccount);

        model.addAttribute("fileFormats", fileFormatService.findAll());

        return SiteViews.FILE;
    }

}
