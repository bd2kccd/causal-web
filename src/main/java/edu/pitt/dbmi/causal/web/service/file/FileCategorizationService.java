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
package edu.pitt.dbmi.causal.web.service.file;

import edu.pitt.dbmi.causal.web.model.file.FileCategorizationForm;
import edu.pitt.dbmi.ccd.db.code.DataDelimiterCodes;
import edu.pitt.dbmi.ccd.db.code.FileFormatCodes;
import edu.pitt.dbmi.ccd.db.code.VariableTypeCodes;
import edu.pitt.dbmi.ccd.db.entity.DataDelimiter;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.TetradVariableFile;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataDelimiterService;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.data.Delimiter;
import edu.pitt.dbmi.data.reader.BasicDataFileReader;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 21, 2018 4:20:24 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileCategorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCategorizationService.class);

    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final VariableTypeService variableTypeService;
    private final DataDelimiterService dataDelimiterService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;
    private final FileManagementService fileManagementService;

    @Autowired
    public FileCategorizationService(FileService fileService, FileFormatService fileFormatService, VariableTypeService variableTypeService, DataDelimiterService dataDelimiterService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService, FileManagementService fileManagementService) {
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.variableTypeService = variableTypeService;
        this.dataDelimiterService = dataDelimiterService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
        this.fileManagementService = fileManagementService;
    }

    public FileFormat categorizeFile(FileCategorizationForm fileCategorizationForm, File file, UserAccount userAccount) {
        FileFormat fileFormat = fileFormatService.findByCode(fileCategorizationForm.getFormatOpt());
        if (fileFormat == null) {
        } else {
            FileFormat prevFileFmt = file.getFileFormat();
            if (prevFileFmt != null && prevFileFmt.getCode() == fileFormat.getCode()) {
                if (fileFormat.getCode() != FileFormatCodes.TETRAD_TAB) {
                    return fileFormat;
                }
            }

            switch (fileFormat.getCode()) {
                case FileFormatCodes.TETRAD_TAB:
                    tetradDataFileService.save(createTetradDataFile(fileCategorizationForm, file, userAccount));
                    break;
                case FileFormatCodes.TETRAD_VAR:
                    tetradVariableFileService.save(createTetradVariableFile(file, userAccount));
                    break;
                default:
                    fileService.changeFileFormat(file, fileFormat);
            }
        }

        return fileFormat;
    }

    private TetradVariableFile createTetradVariableFile(File file, UserAccount userAccount) {
        TetradVariableFile variableFile = new TetradVariableFile();
        variableFile.setFile(file);
        variableFile.setUserAccount(userAccount);

        Path localVarFile = fileManagementService.getUserFile(file, userAccount);
        Delimiter fileDelimiter = Delimiter.TAB;
        try {
            BasicDataFileReader fileReader = new BasicDataFileReader(localVarFile.toFile(), fileDelimiter);
            variableFile.setNumOfVars(fileReader.getNumberOfLines());
        } catch (IOException exception) {
            String errMsg = String.format("Unable get counts for number of variables for file '%s'.", localVarFile.toString());
            LOGGER.error(errMsg, exception);
        }

        return variableFile;
    }

    private TetradDataFile createTetradDataFile(FileCategorizationForm fileCategorizationForm, File file, UserAccount userAccount) {
        boolean hasHeader = fileCategorizationForm.isHasHeader();
        short delimCode = fileCategorizationForm.getDelimOpt();
        short varCode = fileCategorizationForm.getVarOpt();
        Character quoteChar = fileCategorizationForm.getQuoteChar();
        String missValMark = fileCategorizationForm.getMissingValueMarker();
        String cmntMark = fileCategorizationForm.getCommentMarker();

        DataDelimiter dataDelim = dataDelimiterService.findByCode(delimCode);
        VariableType varType = variableTypeService.findByCode(varCode);

        TetradDataFile dataFile = new TetradDataFile();
        dataFile.setCommentMarker(cmntMark);
        dataFile.setDataDelimiter(dataDelim);
        dataFile.setFile(file);
        dataFile.setHasHeader(hasHeader);
        dataFile.setMissingMarker(missValMark);
        dataFile.setQuoteChar(quoteChar);
        dataFile.setVariableType(varType);
        dataFile.setUserAccount(userAccount);

        Path localDataFile = fileManagementService.getUserFile(file, userAccount);
        try {
            BasicDataFileReader fileReader = new BasicDataFileReader(localDataFile.toFile(), getDelimiter(dataDelim));
            dataFile.setNumOfVars(fileReader.getNumberOfColumns());
            dataFile.setNumOfCases(hasHeader ? fileReader.getNumberOfLines() - 1 : fileReader.getNumberOfLines());
        } catch (IOException exception) {
            String errMsg = String.format("Unable to get row and column counts from file %s.", localDataFile.toString());
            LOGGER.error(errMsg, exception);
        }

        return dataFile;
    }

    private Delimiter getDelimiter(DataDelimiter dataDelimiter) {
        if (dataDelimiter == null) {
            return null;
        }

        switch (dataDelimiter.getCode()) {
            case DataDelimiterCodes.COLON:
                return Delimiter.COLON;
            case DataDelimiterCodes.COMMA:
                return Delimiter.COMMA;
            case DataDelimiterCodes.PIPE:
                return Delimiter.PIPE;
            case DataDelimiterCodes.SEMICOLON:
                return Delimiter.SEMICOLON;
            case DataDelimiterCodes.SPACE:
                return Delimiter.SPACE;
            case DataDelimiterCodes.TAB:
                return Delimiter.TAB;
            case DataDelimiterCodes.WHITESPACE:
                return Delimiter.WHITESPACE;
            default:
                return null;
        }
    }

    public FileCategorizationForm createForm(File file) {
        FileCategorizationForm form = new FileCategorizationForm(true);
        form.setVarOpt(VariableTypeCodes.CONTINUOUS);
        form.setDelimOpt(DataDelimiterCodes.TAB);
        form.setQuoteChar('"');
        form.setMissingValueMarker("*");
        form.setCommentMarker("#");

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat == null) {
            form.setFormatOpt(FileFormatCodes.TETRAD_TAB);
        } else {
            form.setFormatOpt(fileFormat.getCode());
            if (fileFormat.getCode() == FileFormatCodes.TETRAD_TAB) {
                TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
                if (dataFile != null) {
                    form.setVarOpt(dataFile.getVariableType().getCode());
                    form.setDelimOpt(dataFile.getDataDelimiter().getCode());
                    form.setQuoteChar(dataFile.getQuoteChar());
                    form.setMissingValueMarker(dataFile.getMissingMarker());
                    form.setCommentMarker(dataFile.getCommentMarker());
                    form.setHasHeader(dataFile.isHasHeader());
                }
            }
        }

        return form;
    }

}
