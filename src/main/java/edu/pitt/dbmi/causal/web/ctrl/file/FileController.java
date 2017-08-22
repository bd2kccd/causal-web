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
import edu.pitt.dbmi.causal.web.model.file.FileCategorizeForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.FileCtrlService;
import edu.pitt.dbmi.causal.web.service.file.FileManagementService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterTypeService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
 * Jun 26, 2017 5:14:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file")
public class FileController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private static final String UNCATEGORIZED_FILE_FORMAT_NAME = "uncategorized";

    private final FileService fileService;
    private final FileTypeService fileTypeService;
    private final FileDelimiterTypeService fileDelimiterTypeService;
    private final FileVariableTypeService fileVariableTypeService;
    private final FileManagementService fileManagementService;
    private final FileCtrlService fileCtrlService;
    private final AppUserService appUserService;

    @Autowired
    public FileController(FileService fileService, FileTypeService fileTypeService, FileDelimiterTypeService fileDelimiterTypeService, FileVariableTypeService fileVariableTypeService, FileManagementService fileManagementService, FileCtrlService fileCtrlService, AppUserService appUserService) {
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileDelimiterTypeService = fileDelimiterTypeService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.fileManagementService = fileManagementService;
        this.fileCtrlService = fileCtrlService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST)
    public String categorizeFile(
            @Valid @ModelAttribute("fileCategorizeForm") final FileCategorizeForm fileCategorizeForm,
            final BindingResult bindingResult,
            @PathVariable final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileCategorizeForm", bindingResult);
            redirAttrs.addFlashAttribute("fileCategorizeForm", fileCategorizeForm);

            return REDIRECT_FILE_INFO + id;
        }
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        fileCtrlService.categorizeFile(fileCategorizeForm, file, userAccount);

        FileFormat fileFmt = file.getFileFormat();
        String fileFmtName = (fileFmt == null) ? "uncategorized" : fileFmt.getName();

        return "redirect:/secured/file/format/" + fileFmtName;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public String showFileInfoAndCategory(@PathVariable final Long id, final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("file", file);
        model.addAttribute("additionalInfo", fileCtrlService.getAdditionalInfo(file));
        model.addAttribute("fileFormatGroupOpts", fileCtrlService.getFileFormatGroupOpts());
        model.addAttribute("tetradTabFileFormatId", fileCtrlService.getTetradTabFileFormatId());
        model.addAttribute("fileTypes", fileTypeService.findAll());
        model.addAttribute("fileDelimiterTypes", fileDelimiterTypeService.findAll());
        model.addAttribute("fileVariableTypes", fileVariableTypeService.findAll());

        if (!model.containsAttribute("fileCategorizeForm")) {
            model.addAttribute("fileCategorizeForm", fileCtrlService.toFileCategorizeForm(file));
        }

        return FILE_INFO_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "title", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateFileTitle(
            @RequestParam(value = "pk", required = true) final Long id,
            @RequestParam(value = "value", required = true) final String title,
            final AppUser appUser) {
        if (id == null) {
            return ResponseEntity.badRequest().body("File ID is required.");
        }
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required.");
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        if (!title.equals(file.getTitle())) {
            if (fileService.getRepository().existsByTitleAndUserAccount(title, userAccount)) {
                return ResponseEntity.badRequest().body("Title already in used. Plese enter a different title.");
            } else {
                file.setTitle(title);
                try {
                    fileService.getRepository().save(file);
                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to update file title.");
                }
            }
        }

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @RequestMapping(value = "format/list/{fileFormatName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listCategorizedFiles(final @PathVariable("fileFormatName") String fileFormatName, final AppUser appUser) {
        if (UNCATEGORIZED_FILE_FORMAT_NAME.equals(fileFormatName)) {
            UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

            return ResponseEntity.ok(fileCtrlService.listUncategorizedFiles(userAccount));
        } else {
            FileFormat fileFormat = fileCtrlService.findSupportedFormat(fileFormatName);
            if (fileFormat == null) {
                return ResponseEntity.notFound().build();
            }

            UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

            return ResponseEntity.ok(fileCtrlService.listCategorizedFiles(fileFormat, userAccount));
        }
    }

    @RequestMapping(value = "format/{fileFormatName}", method = RequestMethod.GET)
    public String showCategorizedFiles(@PathVariable("fileFormatName") String fileFormatName, final Model model) {
        if (UNCATEGORIZED_FILE_FORMAT_NAME.equals(fileFormatName)) {
            model.addAttribute("fileFormat", null);
        } else {
            FileFormat fileFormat = fileCtrlService.findSupportedFormat(fileFormatName);
            if (fileFormat == null) {
                throw new ResourceNotFoundException();
            }

            model.addAttribute("fileFormat", fileFormat);
        }

        return FILE_LIST_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFilesGroupByFileFormat(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        fileManagementService.syncFilesWithDatabase(userAccount);

        model.addAttribute("fileFormatGroups", fileCtrlService.getFileGroupInfos(userAccount));

        return FILE_VIEW;
    }

}
