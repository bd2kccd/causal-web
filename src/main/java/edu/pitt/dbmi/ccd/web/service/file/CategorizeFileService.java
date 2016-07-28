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
package edu.pitt.dbmi.ccd.web.service.file;

import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiter;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableFile;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CategorizeFile;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 *
 * Jul 15, 2016 12:46:31 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class CategorizeFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategorizeFileService.class);

    private final UserAccountService userAccountService;
    private final FileService fileService;
    private final FileTypeService fileTypeService;
    private final FileDelimiterService fileDelimiterService;
    private final VariableTypeService variableTypeService;
    private final DataFileService dataFileService;
    private final VariableFileService variableFileService;

    private final FileType defaultFileType;
    private final FileDelimiter tabDelimiter;
    private final FileDelimiter commaDelimiter;
    private final VariableType defaultVariableType;

    @Autowired
    public CategorizeFileService(UserAccountService userAccountService, FileService fileService, FileTypeService fileTypeService, FileDelimiterService fileDelimiterService, VariableTypeService variableTypeService, DataFileService dataFileService, VariableFileService variableFileService) {
        this.userAccountService = userAccountService;
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.variableTypeService = variableTypeService;
        this.dataFileService = dataFileService;
        this.variableFileService = variableFileService;

        this.defaultFileType = fileTypeService.findByName(FileTypeService.DATA_TYPE_NAME);
        this.tabDelimiter = fileDelimiterService.findByName(FileDelimiterService.TAB_DELIM_NAME);
        this.commaDelimiter = fileDelimiterService.findByName(FileDelimiterService.COMMA_DELIM_NAME);
        this.defaultVariableType = variableTypeService.findByName(VariableTypeService.CONTINUOUS_VAR_NAME);
    }

    public boolean categorizeFile(Long fileId, AppUser appUser, CategorizeFile categorizeFile) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }
        File file = fileService.findByIdAndUserAccount(fileId, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }
        FileType fileType = fileTypeService.findById(categorizeFile.getFileTypeId());
        if (file == null || fileType == null) {
            throw new ResourceNotFoundException();
        }

        String fileTypeName = fileType.getName();
        switch (fileTypeName) {
            case FileTypeService.DATA_TYPE_NAME:
                FileDelimiter fileDelimiter = fileDelimiterService.findById(categorizeFile.getFileDelimiterId());
                VariableType variableType = variableTypeService.findById(categorizeFile.getVariableTypeId());
                if (fileDelimiter == null || variableType == null) {
                    throw new ResourceNotFoundException();
                }
                return changeToDataFileType(file, fileDelimiter, variableType);
            case FileTypeService.VAR_TYPE_NAME:
                return changeToVariableFileType(file);
            default:
                return changeFileType(file, fileType);
        }
    }

    private boolean changeFileType(File file, FileType fileType) {
        boolean success = true;
        try {
            fileService.changeFileType(file, fileType);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            success = false;
        }

        return success;
    }

    private boolean changeToVariableFileType(File file) {
        boolean success = true;
        try {
            Path localFile = Paths.get(file.getAbsolutePath(), file.getName());
            int numOfVars = FileInfos.countLine(localFile.toFile());

            VariableFile variableFile = new VariableFile();
            variableFile.setFile(file);
            variableFile.setNumOfVars(numOfVars);

            variableFileService.save(variableFile);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            success = false;
        }

        return success;
    }

    private boolean changeToDataFileType(File file, FileDelimiter fileDelimiter, VariableType variableType) {
        boolean success = true;
        try {
            char delimiter = FileInfos.delimiterNameToChar(fileDelimiter.getName());
            Path localFile = Paths.get(file.getAbsolutePath(), file.getName());
            int numOfRows = FileInfos.countLine(localFile.toFile());
            int numOfCols = FileInfos.countColumn(localFile.toFile(), delimiter);

            dataFileService.save(new DataFile(file, fileDelimiter, variableType, numOfRows, numOfCols));
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            success = false;
        }

        return success;
    }

    public void showFileCategorizationOptions(File file, Model model) {
        if (!model.containsAttribute("categorizeFile")) {
            model.addAttribute("categorizeFile", createCategorizeFile(file));
        }
        if (!model.containsAttribute("collapse")) {
            model.addAttribute("collapse", Boolean.FALSE);
        }

        model.addAttribute("fileTypes", fileTypeService.findAll());
        model.addAttribute("fileDelimiters", fileDelimiterService.findAll());
        model.addAttribute("variableTypes", variableTypeService.findAll());
    }

    private CategorizeFile createCategorizeFile(File file) {
        FileType fileType = file.getFileType();
        FileDelimiter fileDelimiter = null;
        VariableType variableType = null;

        FileDelimiter defaultDelimiter = file.getName().endsWith(".csv")
                ? commaDelimiter
                : tabDelimiter;

        DataFile dataFile = dataFileService.findByFile(file);
        if (dataFile != null) {
            fileDelimiter = dataFile.getFileDelimiter();
            variableType = dataFile.getVariableType();
        }

        CategorizeFile categorizeFile = new CategorizeFile();
        categorizeFile.setFileTypeId((fileType == null) ? defaultFileType.getId() : fileType.getId());
        categorizeFile.setFileDelimiterId((fileDelimiter == null) ? defaultDelimiter.getId() : fileDelimiter.getId());
        categorizeFile.setVariableTypeId((variableType == null) ? defaultVariableType.getId() : variableType.getId());

        return categorizeFile;
    }

}
