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
import java.util.List;
import java.util.Optional;
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
        Optional<FileFormat> fileFormat = fileFormatService.findById(fileCategorizeForm.getFileFormatId());
        if (fileFormat.isPresent()) {
            FileFormat prevFileFormat = file.getFileFormat();
            if (prevFileFormat != null && prevFileFormat.getId().equals(fileFormat.get().getId())) {
                if (!fileFormat.get().getShortName().equals(FileFormatService.TETRAD_TAB_SHORT_NAME)) {
                    return fileFormat.get();
                }
            }

            switch (fileFormat.get().getShortName()) {
                case FileFormatService.TETRAD_TAB_SHORT_NAME:
                    tetradDataFileService.save(createTetradDataFile(fileCategorizeForm, file, userAccount));
                    break;
                case FileFormatService.TETRAD_VAR_SHORT_NAME:
                    tetradVariableFileService.save(createTetradVariableFile(file, userAccount));
                    break;
                default:
                    fileService.changeFileFormat(file, fileFormat.get());
            }
        }

        return fileFormat.get();
    }

    public FileCategorizeForm createForm(File file) {
        FileCategorizeForm form = new FileCategorizeForm(true);

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat != null && fileFormat.getShortName().equals(FileFormatService.TETRAD_TAB_SHORT_NAME)) {
            TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
            if (dataFile != null) {
                form.setVariableTypeId(dataFile.getVariableType().getId());
                form.setDataDelimiterId(dataFile.getDataDelimiter().getId());
                form.setQuoteChar(dataFile.getQuoteChar());
                form.setMissingValueMarker(dataFile.getMissingMarker());
                form.setCommentMarker(dataFile.getCommentMarker());
                form.setHasHeader(dataFile.isHasHeader());
            }
        } else {
            List<VariableType> variableTypes = variableTypeService.findAll();
            if (!variableTypes.isEmpty()) {
                form.setVariableTypeId(variableTypes.get(0).getId());
            }

            List<DataDelimiter> dataDelimiters = dataDelimiterService.findAll();
            if (!dataDelimiters.isEmpty()) {
                form.setDataDelimiterId(dataDelimiters.get(0).getId());
            }

            form.setQuoteChar('"');
            form.setMissingValueMarker("*");
            form.setCommentMarker("#");
        }

        if (fileFormat == null) {
            fileFormat = fileFormatService.findByShortName(FileFormatService.TETRAD_TAB_SHORT_NAME);
        }

        form.setFileFormatId(fileFormat.getId());

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

        Optional<DataDelimiter> dataDelim = dataDelimiterService.findById(delimId);
        Optional<VariableType> varType = variableTypeService.findById(varId);

        TetradDataFile dataFile = new TetradDataFile();
        dataFile.setCommentMarker(cmntMark);
        dataFile.setDataDelimiter(dataDelim.isPresent() ? dataDelim.get() : null);
        dataFile.setFile(file);
        dataFile.setHasHeader(hasHeader);
        dataFile.setMissingMarker(missValMark);
        dataFile.setQuoteChar(quoteChar);
        dataFile.setVariableType(varType.isPresent() ? varType.get() : null);
        dataFile.setUserAccount(userAccount);

        Path localDataFile = fileManagementService.getLocalFile(file, userAccount);
        try {
            BasicDataFileReader fileReader = new BasicDataFileReader(localDataFile.toFile(), getDelimiter(dataDelim.isPresent() ? dataDelim.get() : null));
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

        switch (dataDelimiter.getShortName()) {
            case DataDelimiterService.COLON_DELIM_SHORT_NAME:
                return Delimiter.COLON;
            case DataDelimiterService.COMMA_DELIM_SHORT_NAME:
                return Delimiter.COMMA;
            case DataDelimiterService.PIPE_DELIM_SHORT_NAME:
                return Delimiter.PIPE;
            case DataDelimiterService.SEMICOLON_DELIM_SHORT_NAME:
                return Delimiter.SEMICOLON;
            case DataDelimiterService.SPACE_DELIM_SHORT_NAME:
                return Delimiter.SPACE;
            case DataDelimiterService.TAB_DELIM_SHORT_NAME:
                return Delimiter.TAB;
            case DataDelimiterService.WHITESPACE_DELIM_SHORT_NAME:
                return Delimiter.WHITESPACE;
            default:
                return null;
        }
    }

}
