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
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterTypeService;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CategorizeFileForm;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 24, 2016 7:38:41 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/mgmt")
public class FileManagementController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagementController.class);

    private final FileManagementService fileManagementService;
    private final FileService fileService;
    private final FileTypeService fileTypeService;
    private final FileFormatService fileFormatService;
    private final FileDelimiterTypeService fileDelimiterTypeService;
    private final FileVariableTypeService fileVariableTypeService;
    private final TetradDataFileService tetradDataFileService;
    private final AppUserService appUserService;

    @Autowired
    public FileManagementController(FileManagementService fileManagementService, FileService fileService, FileTypeService fileTypeService, FileFormatService fileFormatService, FileDelimiterTypeService fileDelimiterTypeService, FileVariableTypeService fileVariableTypeService, TetradDataFileService tetradDataFileService, AppUserService appUserService) {
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileFormatService = fileFormatService;
        this.fileDelimiterTypeService = fileDelimiterTypeService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.tetradDataFileService = tetradDataFileService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ResponseBody
    @RequestMapping(value = "title", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFiles(
            @RequestParam(value = "pk") final Long id,
            @RequestParam(value = "value") final String title,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = getUserFileByFileId(id, userAccount);

        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body("Title is required.");
        } else {
            if (file.getTitle().compareTo(title) != 0) {
                if (fileManagementService.existTitle(title, userAccount)) {
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
        }

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ResponseEntity<?> deleteFile(
            @RequestParam(value = "id") final Long id,
            final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = getUserFileByFileId(id, userAccount);
        if (file == null) {
            return ResponseEntity.notFound().build();
        } else {
            try {
                fileManagementService.deleteFile(file, userAccount);
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete file.");
            }

            return ResponseEntity.ok(id);
        }
    }

    @RequestMapping(value = "categorize", method = RequestMethod.POST)
    public String categorizeFile(
            @Valid @ModelAttribute("categorizeFileForm") final CategorizeFileForm categorizeFileForm,
            final BindingResult bindingResult,
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = getUserFileByFileId(id, userAccount);

        FileFormat fileFormat;
        FileType fileType = fileTypeService.getRepository().findOne(categorizeFileForm.getFileTypeId());
        switch (fileType.getName()) {
            case FileTypeService.DATA:
                fileFormat = fileFormatService.getRepository().findOne(categorizeFileForm.getDataFileFormatId());
                break;
            case FileTypeService.KNOWLEDGE:
                fileFormat = fileFormatService.getRepository().findOne(categorizeFileForm.getKnowledgeFileFormatId());
                break;
            case FileTypeService.VARIABLE:
                fileFormat = fileFormatService.getRepository().findOne(categorizeFileForm.getVariableFileFormatId());
                break;
            default:
                fileFormat = null;
        }

        String fileFmtName = (fileFormat == null) ? "" : fileFormat.getName();
        switch (fileFmtName) {
            case FileFormatService.TETRAD_TABULAR:
                file = fileManagementService.createTetradDataFileAssociation(categorizeFileForm, file, userAccount);
                break;
            case FileFormatService.TETRAD_VARIABLE:
                file = fileManagementService.createTetradVariableFileAssociation(categorizeFileForm, file, userAccount);
                break;
            default:
                file = fileService.updateFileFormat(file, fileFormat);
        }

        return getRedirect(file);
    }

    @RequestMapping(value = "categorize", method = RequestMethod.GET)
    public String categorizeFile(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = getUserFileByFileId(id, userAccount);

        List<FileFormat> fileFormats = fileFormatService.findAll();

        if (!model.containsAttribute("categorizeFileForm")) {
            model.addAttribute("categorizeFileForm", getDefaultCategorizeFileForm(file, fileFormats));
        }

        model.addAttribute("file", file);
        model.addAttribute("addInfo", fileManagementService.getAdditionalInformation(file));
        model.addAttribute("collapse", file.getFileFormat() != null);
        model.addAttribute("fileTypes", filterFileType(fileTypeService.findAll(), FileTypeService.RESULT));
        model.addAttribute("dataFileFormats", extractFileFormat(fileFormats, FileTypeService.DATA));
        model.addAttribute("knwlFileFormats", extractFileFormat(fileFormats, FileTypeService.KNOWLEDGE));
        model.addAttribute("varFileFormats", extractFileFormat(fileFormats, FileTypeService.VARIABLE));
        model.addAttribute("fileDelimiterTypes", fileDelimiterTypeService.findAll());
        model.addAttribute("fileVariableTypes", fileVariableTypeService.findAll());

        return CATEGORIZED_FILE_VIEW;
    }

    private List<FileType> filterFileType(List<FileType> fileTypes, String fileTypeName) {
        return fileTypes.stream()
                .filter(fileType -> !fileType.getName().equals(fileTypeName))
                .collect(Collectors.toList());
    }

    private List<FileFormat> extractFileFormat(List<FileFormat> fileFormats, String fileTypeName) {
        return fileFormats.stream()
                .filter(fileFormat -> fileFormat.getFileType().getName().equals(fileTypeName))
                .collect(Collectors.toList());
    }

    private File getUserFileByFileId(Long id, UserAccount userAccount) {
        if (id == null || userAccount == null) {
            throw new ResourceNotFoundException();
        }

        File file = fileService.getRepository().findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        } else {
            return file;
        }
    }

    private CategorizeFileForm getDefaultCategorizeFileForm(File file, List<FileFormat> fileFormats) {
        CategorizeFileForm form = new CategorizeFileForm();

        // set default file format ids
        List<FileFormat> list = extractFileFormat(fileFormats, FileTypeService.DATA);
        if (!list.isEmpty()) {
            form.setDataFileFormatId(list.get(0).getId());
        }
        list = extractFileFormat(fileFormats, FileTypeService.KNOWLEDGE);
        if (!list.isEmpty()) {
            form.setKnowledgeFileFormatId(list.get(0).getId());
        }
        list = extractFileFormat(fileFormats, FileTypeService.VARIABLE);
        if (!list.isEmpty()) {
            form.setVariableFileFormatId(list.get(0).getId());
        }

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat == null) {
            fileFormat = fileFormatService.findByName(FileFormatService.TETRAD_TABULAR);
        }

        switch (fileFormat.getName()) {
            case FileFormatService.TETRAD_TABULAR:
                TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
                if (dataFile != null) {
                    form.setFileDelimiterTypeId(dataFile.getFileDelimiterType().getId());
                    form.setFileVariableTypeId(dataFile.getFileVariableType().getId());
                    form.setCommentMarker(dataFile.getCommentMarker());
                    form.setMissingValueMarker(dataFile.getMissingValueMarker());
                    form.setQuoteChar(dataFile.getQuoteChar());
                }
                break;
        }

        FileType fileType = fileFormat.getFileType();
        form.setFileTypeId(fileType.getId());
        switch (fileType.getName()) {
            case FileTypeService.DATA:
                form.setDataFileFormatId(fileFormat.getId());
                break;
            case FileTypeService.KNOWLEDGE:
                form.setKnowledgeFileFormatId(fileFormat.getId());
                break;
            case FileTypeService.VARIABLE:
                form.setVariableFileFormatId(fileFormat.getId());
                break;
        }

        return form;
    }

    private String getRedirect(File file) {
        FileFormat fileFmt = file.getFileFormat();
        String fileFmtName = (fileFmt == null) ? "uncategorized" : fileFmt.getName();

        return String.format("redirect:/secured/file/%s", fileFmtName);
    }

}
