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
import edu.pitt.dbmi.causal.web.model.file.FileGroupForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.GroupFileService;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
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
 * Jun 29, 2017 10:57:02 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/group")
public class FileGroupController {

    private final AppUserService appUserService;
    private final VariableTypeService variableTypeService;
    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final GroupFileService groupFileService;

    @Autowired
    public FileGroupController(AppUserService appUserService, VariableTypeService variableTypeService, FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, GroupFileService groupFileService) {
        this.appUserService = appUserService;
        this.variableTypeService = variableTypeService;
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.groupFileService = groupFileService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping("{id}")
    public String updateFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @PathVariable final Long id,
            final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return SitePaths.REDIRECT_FILEGROUP + "/" + id;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (fileGroup == null) {
            throw new ResourceNotFoundException();
        }

        // ensure unique group name
        if (!fileGroup.getName().equalsIgnoreCase(fileGroupForm.getName())
                && fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return SitePaths.REDIRECT_FILEGROUP + "/" + id;
        }

        try {
            groupFileService.saveFileGroup(fileGroupForm, fileGroup, userAccount);
        } catch (ResourceNotFoundException exception) {
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);
            redirAttrs.addFlashAttribute("errorMsg", exception.getMessage());

            return SitePaths.REDIRECT_NEW_FILEGROUP;
        }

        return SitePaths.REDIRECT_FILEGROUP + "/" + fileGroup.getId();
    }

    @GetMapping("{id}")
    public String showFileGroup(@PathVariable final Long id, final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (fileGroup == null) {
            throw new ResourceNotFoundException();
        }

        // add form if not exists
        if (!model.containsAttribute("fileGroupForm")) {
            model.addAttribute("fileGroupForm", groupFileService.createFileGroupForm(fileGroup));
        }

        model.addAttribute("fileGroup", fileGroup);
        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("dataGroups", tetradDataFileService.getFileGroupedByVariableTypeId(userAccount));

        return SiteViews.FILEGROUP_DETAIL;
    }

    @PostMapping("new")
    public String addNewFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return SitePaths.REDIRECT_NEW_FILEGROUP;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        // ensure unique group name
        if (fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return SitePaths.REDIRECT_NEW_FILEGROUP;
        }

        try {
            FileGroup fileGroup = groupFileService.addNewFileGroup(fileGroupForm, userAccount);

            return SitePaths.REDIRECT_FILEGROUP + "/" + fileGroup.getId();
        } catch (ResourceNotFoundException exception) {
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);
            redirAttrs.addFlashAttribute("errorMsg", exception.getMessage());

            return SitePaths.REDIRECT_NEW_FILEGROUP;
        }
    }

    @GetMapping("new")
    public String showNewFileGroup(final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        // add form if not exists
        if (!model.containsAttribute("fileGroupForm")) {
            model.addAttribute("fileGroupForm", groupFileService.createFileGroupForm());
        }

        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("dataGroups", tetradDataFileService.getFileGroupedByVariableTypeId(userAccount));

        return SiteViews.FILEGROUP_NEW;
    }

    @GetMapping
    public String showFileGroupList(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return SiteViews.FILEGROUP;
    }

}
