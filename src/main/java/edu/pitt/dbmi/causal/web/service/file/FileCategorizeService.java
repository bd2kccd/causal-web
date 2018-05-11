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

import edu.pitt.dbmi.causal.web.model.file.FileCategorizeForm;
import edu.pitt.dbmi.causal.web.service.filesys.FileManagementService;
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
public class FileCategorizeService {

    private final long TETRAD_TAB_FORMAT = FileFormatService.TETRAD_TAB_ID;
    private final long TETRAD_VAR_FORMAT = FileFormatService.TETRAD_VAR_ID;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCategorizeService.class);

    private final FileFormatService fileFormatService;
    private final VariableTypeService variableTypeService;
    private final DataDelimiterService dataDelimiterService;
    private final FileService fileService;
    private final FileManagementService fileManagementService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;

    @Autowired
    public FileCategorizeService(FileFormatService fileFormatService, VariableTypeService variableTypeService, DataDelimiterService dataDelimiterService, FileService fileService, FileManagementService fileManagementService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService) {
        this.fileFormatService = fileFormatService;
        this.variableTypeService = variableTypeService;
        this.dataDelimiterService = dataDelimiterService;
        this.fileService = fileService;
        this.fileManagementService = fileManagementService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
    }

    public FileFormat categorizeFile(FileCategorizeForm fileCategorizeForm, File file, UserAccount userAccount) {
        FileFormat fileFmt = fileFormatService.findById(fileCategorizeForm.getFileFormatId());
        if (fileFmt == null) {
            fileService.changeFileFormat(file, fileFmt);
        } else {
            long fileFmtId = fileFmt.getId();

            FileFormat prevFileFmt = file.getFileFormat();
            if (prevFileFmt != null && prevFileFmt.getId() == fileFmtId) {
                if (fileFmtId != TETRAD_TAB_FORMAT) {
                    return fileFmt;
                }
            }

            if (fileFmtId == TETRAD_TAB_FORMAT) {
                tetradDataFileService.save(createTetradDataFile(fileCategorizeForm, file, userAccount));
            } else if (fileFmtId == TETRAD_VAR_FORMAT) {
                tetradVariableFileService.save(createTetradVariableFile(file, userAccount));
            } else {
                fileService.changeFileFormat(file, fileFmt);
            }
        }

        return fileFmt;
    }

    public FileCategorizeForm createForm(File file) {
        FileCategorizeForm form = new FileCategorizeForm(true);
        form.setVariableTypeId(VariableTypeService.CONTINUOUS_ID);
        form.setDataDelimiterId(DataDelimiterService.TAB_DELIM_ID);
        form.setQuoteChar('"');
        form.setMissingValueMarker("*");
        form.setCommentMarker("#");

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat == null) {
            form.setFileFormatId(FileFormatService.TETRAD_TAB_ID);
        } else {
            if (fileFormat.getId() == TETRAD_TAB_FORMAT) {
                TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
                if (dataFile != null) {
                    form.setVariableTypeId(dataFile.getVariableType().getId());
                    form.setDataDelimiterId(dataFile.getDataDelimiter().getId());
                    form.setQuoteChar(dataFile.getQuoteChar());
                    form.setMissingValueMarker(dataFile.getMissingMarker());
                    form.setCommentMarker(dataFile.getCommentMarker());
                    form.setHasHeader(dataFile.isHasHeader());
                }
            }
            form.setFileFormatId(fileFormat.getId());
        }

        return form;
    }

    private TetradVariableFile createTetradVariableFile(File file, UserAccount userAccount) {
        TetradVariableFile variableFile = new TetradVariableFile();
        variableFile.setFile(file);
        variableFile.setUserAccount(userAccount);

        Path localVarFile = fileManagementService.getLocalFile(file, userAccount);
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

    private TetradDataFile createTetradDataFile(FileCategorizeForm fileCategorizeForm, File file, UserAccount userAccount) {
        boolean hasHeader = fileCategorizeForm.isHasHeader();
        Long delimId = fileCategorizeForm.getDataDelimiterId();
        Long varId = fileCategorizeForm.getVariableTypeId();
        Character quoteChar = fileCategorizeForm.getQuoteChar();
        String missValMark = fileCategorizeForm.getMissingValueMarker();
        String cmntMark = fileCategorizeForm.getCommentMarker();

        DataDelimiter dataDelim = dataDelimiterService.findById(delimId);
        VariableType varType = variableTypeService.findById(varId);

        TetradDataFile dataFile = new TetradDataFile();
        dataFile.setCommentMarker(cmntMark);
        dataFile.setDataDelimiter(dataDelim);
        dataFile.setFile(file);
        dataFile.setHasHeader(hasHeader);
        dataFile.setMissingMarker(missValMark);
        dataFile.setQuoteChar(quoteChar);
        dataFile.setVariableType(varType);
        dataFile.setUserAccount(userAccount);

        Path localDataFile = fileManagementService.getLocalFile(file, userAccount);
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

        long id = dataDelimiter.getId();
        if (id == DataDelimiterService.COLON_DELIM_ID) {
            return Delimiter.COLON;
        } else if (id == DataDelimiterService.COMMA_DELIM_ID) {
            return Delimiter.COMMA;
        } else if (id == DataDelimiterService.PIPE_DELIM_ID) {
            return Delimiter.PIPE;
        } else if (id == DataDelimiterService.SEMICOLON_DELIM_ID) {
            return Delimiter.SEMICOLON;
        } else if (id == DataDelimiterService.SPACE_DELIM_ID) {
            return Delimiter.SPACE;
        } else if (id == DataDelimiterService.TAB_DELIM_ID) {
            return Delimiter.TAB;
        } else if (id == DataDelimiterService.WHITESPACE_DELIM_ID) {
            return Delimiter.WHITESPACE;
        } else {
            return null;
        }
    }

}
