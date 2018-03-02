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

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.file.FileGroupForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.GroupFileService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FileGroupController.class);

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

    @RequestMapping(value = "{groupId}", method = RequestMethod.POST)
    public String updateFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @PathVariable final Long groupId,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP + groupId;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        FileGroup fileGroup = fileGroupService.getRepository()
                .findByIdAndUserAccount(groupId, userAccount);
        if (fileGroup == null) {
            throw new ResourceNotFoundException();
        }

        if (!fileGroup.getName().equalsIgnoreCase(fileGroupForm.getGroupName())
                && fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getGroupName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP + groupId;
        }

        VariableType varType = variableTypeService.getRepository()
                .findOne(fileGroupForm.getVarTypeId());
        if (varType == null) {
            throw new ResourceNotFoundException();
        }

        List<File> files = tetradDataFileService.getRepository()
                .findByVariableTypeAndAndFileIdsAndUserAccount(varType, fileGroupForm.getFileIds(), userAccount).stream()
                .map(TetradDataFile::getFile)
                .collect(Collectors.toList());
        if (files.isEmpty()) {
            String errMsg = String.format("Please select file(s) for '%s' variable type.", varType.getName());
            bindingResult.rejectValue("fileIds", "fileGroupForm.fileIds", errMsg);
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP + groupId;
        }

        try {
            groupFileService.updateFileGroup(fileGroup, fileGroupForm.getGroupName(), files);
        } catch (Exception exception) {
            LOGGER.error("Unable to update file group.", exception);

            redirAttrs.addFlashAttribute("errorMsg", Collections.singletonList("Failed to update file group."));
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP_NEW;
        }

        return ViewPath.REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "{groupId}", method = RequestMethod.GET)
    public String showFileGroup(@PathVariable final Long groupId, final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        // add form if not exists
        if (!model.containsAttribute("fileGroupForm")) {
            FileGroup fileGroup = fileGroupService.getRepository()
                    .findByIdAndUserAccount(groupId, userAccount);
            if (fileGroup == null) {
                throw new ResourceNotFoundException();
            }

            model.addAttribute("fileGroupForm", groupFileService.createFileGroupForm(fileGroup));
        }

        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("dataGroups", tetradDataFileService.getFileGroupedByVariableTypeId(userAccount));

        return ViewPath.FILEGROUP_VIEW;
    }

    @RequestMapping(value = "new", method = RequestMethod.POST)
    public String addNewFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP_NEW;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getGroupName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP_NEW;
        }

        VariableType varType = variableTypeService.getRepository()
                .findOne(fileGroupForm.getVarTypeId());
        if (varType == null) {
            throw new ResourceNotFoundException();
        }

        List<File> files = tetradDataFileService.getRepository()
                .findByVariableTypeAndAndFileIdsAndUserAccount(varType, fileGroupForm.getFileIds(), userAccount).stream()
                .map(TetradDataFile::getFile)
                .collect(Collectors.toList());
        if (files.isEmpty()) {
            String errMsg = String.format("Please select file(s) for '%s' variable type.", varType.getName());
            bindingResult.rejectValue("fileIds", "fileGroupForm.fileIds", errMsg);
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP_NEW;
        }

        try {
            groupFileService.saveFileGroup(fileGroupForm.getGroupName(), files, userAccount);
        } catch (Exception exception) {
            LOGGER.error("Unable to create new file group.", exception);

            redirAttrs.addFlashAttribute("errorMsg", Collections.singletonList("Failed to create new file group."));
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return ViewPath.REDIRECT_FILEGROUP_NEW;
        }

        return ViewPath.REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String showNewFileGroup(final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        // add form if not exists
        if (!model.containsAttribute("fileGroupForm")) {
            model.addAttribute("fileGroupForm", groupFileService.createFileGroupForm());
        }

        model.addAttribute("varTypes", variableTypeService.findAll());
        model.addAttribute("dataGroups", tetradDataFileService.getFileGroupedByVariableTypeId(userAccount));

        return ViewPath.FILEGROUP_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFileGroupList(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return ViewPath.FILEGROUP_LIST_VIEW;
    }

}
