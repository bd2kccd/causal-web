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
import edu.pitt.dbmi.ccd.db.entity.FileGroup;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.FileGroupForm;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.file.FileGroupingService;
import java.util.LinkedList;
import java.util.List;
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

    private final FileService fileService;
    private final FileGroupService fileGroupService;
    private final TetradDataFileService tetradDataFileService;
    private final FileVariableTypeService fileVariableTypeService;
    private final FileGroupingService fileGroupingService;
    private final AppUserService appUserService;

    @Autowired
    public FileGroupController(FileService fileService, FileGroupService fileGroupService, TetradDataFileService tetradDataFileService, FileVariableTypeService fileVariableTypeService, FileGroupingService fileGroupingService, AppUserService appUserService) {
        this.fileService = fileService;
        this.fileGroupService = fileGroupService;
        this.tetradDataFileService = tetradDataFileService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.fileGroupingService = fileGroupingService;
        this.appUserService = appUserService;
    }

    @ResponseBody
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResponseEntity<?> deleteFile(
            @RequestParam(value = "id") final Long id,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

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

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String saveUpdatedFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (fileGroup == null) {
            throw new ResourceNotFoundException();
        }

        fileGroupingService.updateFileGroup(fileGroupForm, fileGroup, userAccount);

        return REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String updateFileGroup(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        FileGroup fileGroup = fileGroupService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (fileGroup == null) {
            throw new ResourceNotFoundException();
        }

        List<File> files = fileGroup.getFiles();
        List<Long> fileIds = files.stream().map(File::getId).collect(Collectors.toList());

        TetradDataFile tetradDataFile = tetradDataFileService.getRepository().findByFile(files.get(0));

        FileGroupForm fileGroupForm = new FileGroupForm();
        fileGroupForm.setGroupName(fileGroup.getName());
        fileGroupForm.setFileVariableTypeId(tetradDataFile.getFileVariableType().getId());
        fileGroupForm.setFileIds(fileIds);

        model.addAttribute("pageTitle", "Update File Group");
        model.addAttribute("fileGroupForm", fileGroupForm);

        setupFileGroupView(userAccount, model);

        return FILEGROUP_VIEW;
    }

    @RequestMapping(value = "new", method = RequestMethod.POST)
    public String createFileGroup(
            @Valid @ModelAttribute("fileGroupForm") final FileGroupForm fileGroupForm,
            final BindingResult bindingResult,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        if (fileGroupService.getRepository().existsByNameAndUserAccount(fileGroupForm.getGroupName(), userAccount)) {
            bindingResult.rejectValue("groupName", "fileGroupForm.groupName", "Name already existed.");
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileGroupForm", bindingResult);
            redirAttrs.addFlashAttribute("fileGroupForm", fileGroupForm);

            return REDIRECT_FILEGROUP_VIEW;
        } else {
            fileGroupingService.addFileGroup(fileGroupForm, userAccount);
        }

        return REDIRECT_FILEGROUP_LIST;
    }

    @RequestMapping(value = "new", method = RequestMethod.GET)
    public String showFileGroup(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        setupFileGroupView(userAccount, model);

        model.addAttribute("pageTitle", "Update File Group");

        return FILEGROUP_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFileGroupList(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return FILEGROUP_LIST_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ResponseEntity<?> listFileGrops(
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(fileGroupingService.getFileGroups(userAccount));
    }

    private void setupFileGroupView(final UserAccount userAccount, final Model model) {
        List<File> continuousData = new LinkedList<>();
        List<File> discreteData = new LinkedList<>();
        List<File> mixedData = new LinkedList<>();
        List<TetradDataFile> tetradDataFiles = tetradDataFileService.getRepository().findByUserAccount(userAccount);
        tetradDataFiles.forEach(tetradDataFile -> {
            FileVariableType variableType = tetradDataFile.getFileVariableType();
            switch (variableType.getName()) {
                case FileVariableTypeService.CONTINUOUS_NAME:
                    continuousData.add(tetradDataFile.getFile());
                    break;
                case FileVariableTypeService.DISCRETE_NAME:
                    discreteData.add(tetradDataFile.getFile());
                    break;
                case FileVariableTypeService.MIXED_NAME:
                    mixedData.add(tetradDataFile.getFile());
                    break;
            }
        });

        List<FileVariableType> fileVariableTypes = fileVariableTypeService.findAll();

        model.addAttribute("fileVariableTypes", fileVariableTypes);
        model.addAttribute("continuousData", continuousData);
        model.addAttribute("discreteData", discreteData);
        model.addAttribute("mixedData", mixedData);

        if (!model.containsAttribute("fileGroupForm")) {
            FileGroupForm fileGroupForm = new FileGroupForm();

            if (!fileVariableTypes.isEmpty()) {
                fileGroupForm.setFileVariableTypeId(fileVariableTypes.get(0).getId());
            }

            model.addAttribute("fileGroupForm", fileGroupForm);
        }
    }

}
