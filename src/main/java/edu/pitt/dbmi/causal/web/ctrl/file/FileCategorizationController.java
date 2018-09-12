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
import edu.pitt.dbmi.causal.web.model.file.FileCategorizationForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.FileCategorizationService;
import edu.pitt.dbmi.causal.web.service.file.FileDetailService;
import edu.pitt.dbmi.ccd.db.code.FileFormatCodes;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataDelimiterService;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
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
 * May 24, 2018 2:58:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/categorization")
public class FileCategorizationController {

    private final AppUserService appUserService;
    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final DataDelimiterService dataDelimiterService;
    private final VariableTypeService variableTypeService;
    private final FileCategorizationService fileCategorizationService;
    private final FileDetailService fileDetailService;

    @Autowired
    public FileCategorizationController(AppUserService appUserService, FileService fileService, FileFormatService fileFormatService, DataDelimiterService dataDelimiterService, VariableTypeService variableTypeService, FileCategorizationService fileCategorizationService, FileDetailService fileDetailService) {
        this.appUserService = appUserService;
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.dataDelimiterService = dataDelimiterService;
        this.variableTypeService = variableTypeService;
        this.fileCategorizationService = fileCategorizationService;
        this.fileDetailService = fileDetailService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping("{id}")
    public String categorizeFile(
            @Valid @ModelAttribute("fileCategorizationForm") final FileCategorizationForm fileCategorizationForm,
            final BindingResult bindingResult,
            @PathVariable final Long id,
            final Model model,
            final AppUser appUser,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileCategorizationForm", bindingResult);
            redirAttrs.addFlashAttribute("fileCategorizationForm", fileCategorizationForm);

            return SitePaths.REDIRECT_FILE_CATEGORIZATION + "/" + id;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        FileFormat fileFormat = fileCategorizationService.categorizeFile(fileCategorizationForm, file, userAccount);
        if (fileFormat != null) {
            redirAttrs.addFlashAttribute("fileFormatCode", fileFormat.getCode());
        }

        return SitePaths.REDIRECT_FILE_CATEGORIZATION + "/" + id;
    }

    @GetMapping("{id}")
    public String showFileCategorization(@PathVariable final Long id, final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        if (!model.containsAttribute("fileCategorizationForm")) {
            model.addAttribute("fileCategorizationForm", fileCategorizationService.createForm(file));
        }
        model.addAttribute("file", file);
        model.addAttribute("categorizationDetails", fileDetailService.getCategorizationDetails(file));
        model.addAttribute("formatOpts", fileFormatService.getFileFormatOptions());
        model.addAttribute("delimOpts", dataDelimiterService.findAll());
        model.addAttribute("varOpts", variableTypeService.findAll());
        model.addAttribute("tetrad_tab_format_id", FileFormatCodes.TETRAD_TAB);

        return (file.getFileFormat() == null)
                ? SiteViews.FILE_CATEGORIZATION
                : SiteViews.FILE_RECATEGORIZATION;
    }

}
