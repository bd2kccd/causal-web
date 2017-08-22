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
package edu.pitt.dbmi.causal.web.ctrl.file;

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.file.FileGroupForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.FileGroupCtrlService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
public class FileGroupController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileGroupController.class);

    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final FileVariableTypeService fileVariableTypeService;
    private final FileGroupCtrlService fileGroupCtrlService;
    private final AppUserService appUserService;

    @Autowired
    public FileGroupController(FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, FileVariableTypeService fileVariableTypeService, FileGroupCtrlService fileGroupCtrlService, AppUserService appUserService) {
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.fileGroupCtrlService = fileGroupCtrlService;
        this.appUserService = appUserService;
    }

    @ResponseBody
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResponseEntity<?> deleteFile(
            @RequestParam(value = "id") final Long id,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        if (fileGroupService.getRepository().existsByIdAndUserAccount(id, userAccount)) {
            try {
                fileGroupService.getRepository().deleteByIdAndUserAccount(id, userAccount);
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete file group.");
            }

            return ResponseEntity.ok(id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST)
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

            return REDIRECT_FILEGROUP_VIEW + id;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        FileVariableType varType = fileVariableTypeService.getRepository().findOne(fileGroupForm.getVarTypeId());
        if (varType == null) {
            throw new ResourceNotFoundException();
        }

        List<TetradDataFile> dataFiles = tetradDataFileService.getRepository()
                .findByFileVariableTypeAndAndFileIdsAndUserAccount(varType, fileGroupForm.getFileIds(), userAccount);
        if (dataFiles.isEmpty()) {
            String errMsg = String.format("Please select file(s) for '%s' variable type.", varType.getDisplayName());
            bindingResult.rejectValue("fileIds", "fileGroupForm.fileIds", errMsg);
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW + id;
        }

        if (fileGroupService.getRepository().existsByNameAndUserAccountAndIdNot(fileGroupForm.getGroupName(), userAccount, id)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW + id;
        }

        FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (fileGroup == null) {
            throw new ResourceNotFoundException();
        }

        fileGroupCtrlService.updateFileGroup(fileGroupForm.getGroupName(), dataFiles, fileGroup);

        return REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public String showFileGroup(@PathVariable final Long id, final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        List<FileVariableType> varTypes = fileVariableTypeService.findAll();
        Map<Long, List<File>> dataGroups = fileGroupCtrlService.getTetradGroupedData(varTypes, userAccount);

        model.addAttribute("pageTitle", "Update File Group");
        model.addAttribute("varTypes", varTypes);
        model.addAttribute("dataGroups", dataGroups);

        // add form if not exists
        if (!model.containsAttribute("fileGroupForm")) {
            FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(id, userAccount);
            if (fileGroup == null) {
                throw new ResourceNotFoundException();
            }

            List<File> files = fileGroup.getFiles();
            List<Long> fileIds = files.stream().map(File::getId).collect(Collectors.toList());

            TetradDataFile tetradDataFile = tetradDataFileService.getRepository().findByFile(files.get(0));

            FileGroupForm fileGroupForm = new FileGroupForm();
            fileGroupForm.setGroupName(fileGroup.getName());
            fileGroupForm.setVarTypeId(tetradDataFile.getFileVariableType().getId());
            fileGroupForm.setFileIds(fileIds);
            model.addAttribute("fileGroupForm", fileGroupForm);
        }

        return FILEGROUP_VIEW;
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

            return REDIRECT_NEW_FILEGROUP_VIEW;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        if (fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getGroupName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_NEW_FILEGROUP_VIEW;
        }

        FileVariableType varType = fileVariableTypeService.getRepository().findOne(fileGroupForm.getVarTypeId());
        if (varType == null) {
            throw new ResourceNotFoundException();
        }

        List<TetradDataFile> dataFiles = tetradDataFileService.getRepository()
                .findByFileVariableTypeAndAndFileIdsAndUserAccount(varType, fileGroupForm.getFileIds(), userAccount);
        if (dataFiles.isEmpty()) {
            String errMsg = String.format("Please select file(s) for '%s' variable type.", varType.getDisplayName());
            bindingResult.rejectValue("fileIds", "fileGroupForm.fileIds", errMsg);
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_NEW_FILEGROUP_VIEW;
        }

        fileGroupCtrlService.addNewFileGroup(fileGroupForm.getGroupName(), dataFiles, userAccount);

        return REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String showNewFileGroup(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        List<FileVariableType> varTypes = fileVariableTypeService.findAll();
        Map<Long, List<File>> dataGroups = fileGroupCtrlService.getTetradGroupedData(varTypes, userAccount);

        model.addAttribute("pageTitle", "New File Group");
        model.addAttribute("varTypes", varTypes);
        model.addAttribute("dataGroups", dataGroups);

        // add form if not exists
        if (!model.containsAttribute("fileGroupForm")) {
            FileGroupForm fileGroupForm = new FileGroupForm();

            if (!varTypes.isEmpty()) {
                fileGroupForm.setVarTypeId(varTypes.get(0).getId());
            }

            model.addAttribute("fileGroupForm", fileGroupForm);
        }

        return FILEGROUP_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ResponseEntity<?> listFileGrops(
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(fileGroupService.getRepository().findByUserAccount(userAccount));
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFileGroupList(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return FILEGROUP_LIST_VIEW;
    }

}
