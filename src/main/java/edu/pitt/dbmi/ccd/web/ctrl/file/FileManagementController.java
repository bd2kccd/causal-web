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
import edu.pitt.dbmi.ccd.db.entity.FileDelimiterType;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.FileVariableType;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.TetradVariableFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterTypeService;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CategorizeFileForm;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import edu.pitt.dbmi.data.Delimiter;
import edu.pitt.dbmi.data.reader.BasicDataFileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
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
    private final TetradVariableFileService tetradVariableFileService;
    private final AppUserService appUserService;

    @Autowired
    public FileManagementController(
            FileManagementService fileManagementService,
            FileService fileService,
            FileTypeService fileTypeService,
            FileFormatService fileFormatService,
            FileDelimiterTypeService fileDelimiterTypeService,
            FileVariableTypeService fileVariableTypeService,
            TetradDataFileService tetradDataFileService,
            TetradVariableFileService tetradVariableFileService,
            AppUserService appUserService) {
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileFormatService = fileFormatService;
        this.fileDelimiterTypeService = fileDelimiterTypeService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    private String getRedirect(File file) {
        FileFormat fileFmt = file.getFileFormat();
        String fileFmtName = (fileFmt == null) ? "new" : fileFmt.getName();

        return String.format("redirect:/secured/file/mgmt/list/%s", fileFmtName);
    }

    private void removeAssociatedTables(File file) {
        FileFormat fileFmt = file.getFileFormat();
        String fileFmtName = (fileFmt == null) ? null : fileFmt.getName();

        if (!FileFormatService.TETRAD_TABULAR.equals(fileFmtName)) {
            TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
            if (dataFile != null) {
                tetradDataFileService.getRepository().delete(dataFile);
            }
        }
        if (!FileFormatService.TETRAD_VARIABLE.equals(fileFmtName)) {
            TetradVariableFile variableFile = tetradVariableFileService.getRepository().findByFile(file);
            if (variableFile != null) {
                tetradVariableFileService.getRepository().delete(variableFile);
            }
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
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        Long fileFmtId = categorizeFileForm.getFileFormatId();
        FileFormat fileFormat = fileFormatService.getRepository().findOne(fileFmtId);
        if (fileFormat != null) {
            switch (fileFormat.getName()) {
                case FileFormatService.TETRAD_TABULAR:
                    Long fileDelimTypeId = categorizeFileForm.getFileDelimiterTypeId();
                    Long fileVarTypeId = categorizeFileForm.getFileVariableTypeId();
                    Character quoteChar = categorizeFileForm.getQuoteChar();
                    String missValMark = categorizeFileForm.getMissingValueMarker();
                    String cmntMark = categorizeFileForm.getCommentMarker();

                    FileDelimiterType delimiter = fileDelimiterTypeService.getRepository().findOne(fileDelimTypeId);
                    FileVariableType variable = fileVariableTypeService.getRepository().findOne(fileVarTypeId);

                    TetradDataFile dataFile = new TetradDataFile(file, delimiter, variable);
                    dataFile.setCommentMarker(cmntMark);
                    dataFile.setMissingValueMarker(missValMark);
                    dataFile.setQuoteChar(quoteChar);

                    Path localDataFile = fileManagementService.getPhysicalFile(file, userAccount);
                    try {
                        BasicDataFileReader fileReader = new BasicDataFileReader(localDataFile.toFile(), getReaderFileDelimiter(delimiter));
                        dataFile.setNumOfColumns(fileReader.getNumberOfColumns());
                        dataFile.setNumOfRows(fileReader.getNumberOfLines());
                    } catch (IOException exception) {
                        String errMsg = String.format("Unable to get row and column counts from file %s.", localDataFile.getFileName().toString());
                        LOGGER.error(errMsg, exception);
                    }

                    tetradDataFileService.getRepository().save(dataFile);
                    break;
                case FileFormatService.TETRAD_VARIABLE:
                    TetradVariableFile variableFile = new TetradVariableFile(file);
                    Path localVarFile = fileManagementService.getPhysicalFile(file, userAccount);
                    try {
                        BasicDataFileReader fileReader = new BasicDataFileReader(localVarFile.toFile(), getReaderFileDelimiter(null));
                        variableFile.setNumOfVariables(fileReader.getNumberOfLines());
                    } catch (IOException exception) {
                        String errMsg = String.format("Unable to get row counts from file %s.", localVarFile.getFileName().toString());
                        LOGGER.error(errMsg, exception);
                    }

                    tetradVariableFileService.getRepository().save(variableFile);
                    break;
            }
            file.setFileFormat(fileFormat);
            file = fileService.getRepository().save(file);
        }

        removeAssociatedTables(file);

        return getRedirect(file);
    }

    @RequestMapping(value = "categorize", method = RequestMethod.GET)
    public String categorizeFile(
            @RequestParam(value = "id") final Long id,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        if (!model.containsAttribute("categorizeFileForm")) {
            model.addAttribute("categorizeFileForm", getDefaultCategorizeFileForm(file));
        }

        model.addAttribute("file", file);
        model.addAttribute("fileTypes", fileTypeService.getRepository().findAll());
        model.addAttribute("dataFileFormats", fileFormatService.findByFileTypeName(FileTypeService.DATA));
        model.addAttribute("knwlFileFormats", fileFormatService.findByFileTypeName(FileTypeService.KNOWLEDGE));
        model.addAttribute("resultFileFormats", fileFormatService.findByFileTypeName(FileTypeService.RESULT));
        model.addAttribute("varFileFormats", fileFormatService.findByFileTypeName(FileTypeService.VARIABLE));
        model.addAttribute("fileDelimiterTypes", fileDelimiterTypeService.getRepository().findAll());
        model.addAttribute("fileVariableTypes", fileVariableTypeService.getRepository().findAll());

        return CATEGORIZED_FILE_VIEW;
    }

    @RequestMapping(value = "list/{fileType}", method = RequestMethod.GET)
    public String showFiles(@PathVariable String fileType, final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        fileManagementService.syncDatabaseWithDataDirectory(userAccount);

        String pageTitle;
        String title;
        switch (fileType) {
            case FileFormatService.TETRAD_TABULAR:
                pageTitle = "CCD: Tetrad Tabular Data";
                title = "Tetrad Tabular Data Files";
                break;
            case FileFormatService.TETRAD_COVARIANCE:
                pageTitle = "CCD: Tetrad Covariance";
                title = "Tetrad Covariance Files";
                break;
            case FileFormatService.TETRAD_VARIABLE:
                pageTitle = "CCD: Tetrad Variable";
                title = "Tetrad Variable Files";
                break;
            case FileFormatService.TETRAD_KNOWLEDGE:
                pageTitle = "CCD: Tetrad Knowledge";
                title = "Tetrad Knowledge Files";
                break;
            case FileFormatService.TDI_TABULAR:
                pageTitle = "CCD: TDI Tabular Data";
                title = "TDI Tabular Data Files";
                break;
            case "new":
                pageTitle = "CCD: Uncategorize File";
                title = "Uncategorized Files";
                break;
            default:
                throw new ResourceNotFoundException();
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("title", title);
        model.addAttribute("fileType", fileType);

        return FILE_LIST_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list/file/{fileType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFiles(final @PathVariable String fileType, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        List<File> files = new LinkedList<>();
        switch (fileType) {
            case FileFormatService.TETRAD_TABULAR:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_TABULAR, userAccount));
                break;
            case FileFormatService.TETRAD_COVARIANCE:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_COVARIANCE, userAccount));
                break;
            case FileFormatService.TETRAD_VARIABLE:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_VARIABLE, userAccount));
                break;
            case FileFormatService.TETRAD_KNOWLEDGE:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_KNOWLEDGE, userAccount));
                break;
            case FileFormatService.TDI_TABULAR:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TDI_TABULAR, userAccount));
                break;
            case "new":
                files.addAll(fileService.findByUserAccountAndFileFormatName(null, userAccount));
                break;
            default:
                return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(files);
    }

    private CategorizeFileForm getDefaultCategorizeFileForm(File file) {
        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat == null) {
            fileFormat = fileFormatService.getRepository().findByName(FileFormatService.TETRAD_TABULAR);
        }

        CategorizeFileForm form = new CategorizeFileForm();
        form.setFileFormatId(fileFormat.getId());
        form.setFileTypeId(fileFormat.getFileType().getId());

        return form;
    }

    private Delimiter getReaderFileDelimiter(FileDelimiterType fileDelimiterType) {
        String fileDelimName = (fileDelimiterType == null) ? "" : fileDelimiterType.getName();
        switch (fileDelimName) {
            case FileDelimiterTypeService.COLON_DELIM_NAME:
                return Delimiter.COLON;
            case FileDelimiterTypeService.COMMA_DELIM_NAME:
                return Delimiter.COMMA;
            case FileDelimiterTypeService.PIPE_DELIM_NAME:
                return Delimiter.PIPE;
            case FileDelimiterTypeService.SEMICOLON_DELIM_NAME:
                return Delimiter.SEMICOLON;
            case FileDelimiterTypeService.SPACE_DELIM_NAME:
                return Delimiter.SPACE;
            case FileDelimiterTypeService.TAB_DELIM_NAME:
                return Delimiter.TAB;
            case FileDelimiterTypeService.WHITESPACE_DELIM_NAME:
                return Delimiter.WHITESPACE;
            default:
                return Delimiter.TAB;
        }
    }

}
