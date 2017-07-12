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
package edu.pitt.dbmi.ccd.web.service.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiterType;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.FileType;
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
import edu.pitt.dbmi.ccd.web.domain.AttrValue;
import edu.pitt.dbmi.ccd.web.domain.file.FileCategorizeForm;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import edu.pitt.dbmi.data.Delimiter;
import edu.pitt.dbmi.data.reader.BasicDataFileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 10, 2017 11:08:34 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileCategorizeCtrlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCategorizeCtrlService.class);

    private final FileService fileService;
    private final FileTypeService fileTypeService;
    private final FileFormatService fileFormatService;
    private final FileDelimiterTypeService fileDelimiterTypeService;
    private final FileVariableTypeService fileVariableTypeService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;
    private final FileManagementService fileManagementService;

    @Autowired
    public FileCategorizeCtrlService(FileService fileService,
            FileTypeService fileTypeService,
            FileFormatService fileFormatService,
            FileDelimiterTypeService fileDelimiterTypeService,
            FileVariableTypeService fileVariableTypeService,
            TetradDataFileService tetradDataFileService,
            TetradVariableFileService tetradVariableFileService,
            FileManagementService fileManagementService) {
        this.fileService = fileService;
        this.fileTypeService = fileTypeService;
        this.fileFormatService = fileFormatService;
        this.fileDelimiterTypeService = fileDelimiterTypeService;
        this.fileVariableTypeService = fileVariableTypeService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
        this.fileManagementService = fileManagementService;
    }

    private TetradDataFile createTetradDataFile(FileCategorizeForm fileCategorizeForm, File file, UserAccount userAccount) {
        Long fileDelimTypeId = fileCategorizeForm.getFileDelimiterId();
        Long fileVarTypeId = fileCategorizeForm.getVariableTypeId();
        Character quoteChar = fileCategorizeForm.getQuoteChar();
        String missValMark = fileCategorizeForm.getMissingValueMarker();
        String cmntMark = fileCategorizeForm.getCommentMarker();

        FileDelimiterType delimiter = fileDelimiterTypeService.getRepository().findOne(fileDelimTypeId);
        FileVariableType variable = fileVariableTypeService.getRepository().findOne(fileVarTypeId);

        TetradDataFile dataFile = new TetradDataFile(file, delimiter, variable);
        dataFile.setCommentMarker(cmntMark);
        dataFile.setMissingValueMarker(missValMark);
        dataFile.setQuoteChar(quoteChar);

        Path localDataFile = fileManagementService.getPhysicalFile(file, userAccount);
        Delimiter fileDelimiter = fileManagementService.getReaderFileDelimiter(delimiter);
        try {
            BasicDataFileReader fileReader = new BasicDataFileReader(localDataFile.toFile(), fileDelimiter);
            dataFile.setNumOfColumns(fileReader.getNumberOfColumns());
            dataFile.setNumOfRows(fileReader.getNumberOfLines());
        } catch (IOException exception) {
            String errMsg = String.format("Unable to get row and column counts from file %s.", localDataFile.toString());
            LOGGER.error(errMsg, exception);
        }

        return dataFile;
    }

    private TetradVariableFile createTetradVariableFile(FileCategorizeForm fileCategorizeForm, File file, UserAccount userAccount) {
        TetradVariableFile variableFile = new TetradVariableFile(file);
        Path localVarFile = fileManagementService.getPhysicalFile(file, userAccount);
        Delimiter fileDelimiter = fileManagementService.getReaderFileDelimiter(null);
        try {
            BasicDataFileReader fileReader = new BasicDataFileReader(localVarFile.toFile(), fileDelimiter);
            variableFile.setNumOfVariables(fileReader.getNumberOfLines());
        } catch (IOException exception) {
            String errMsg = String.format("Unable get counts for number of variables for file '%s'.", localVarFile.toString());
            LOGGER.error(errMsg, exception);
        }

        return variableFile;
    }

    public File categorizeFile(FileCategorizeForm fileCategorizeForm, File file, UserAccount userAccount) {
        FileFormat fileFormat = fileFormatService.getRepository().findOne(fileCategorizeForm.getFileFormatId());
        switch (fileFormat.getName()) {
            case FileFormatService.TETRAD_TABULAR:
                TetradDataFile dataFile = createTetradDataFile(fileCategorizeForm, file, userAccount);
                dataFile = tetradDataFileService.save(dataFile);

                return dataFile.getFile();
            case FileFormatService.TETRAD_VARIABLE:
                TetradVariableFile variableFile = createTetradVariableFile(fileCategorizeForm, file, userAccount);
                variableFile = tetradVariableFileService.save(variableFile);

                return variableFile.getFile();
            default:
                return fileService.updateFileFormat(file, fileFormat);
        }
    }

    public Long getTetradDataGroupId() {
        FileFormat fileFormat = fileFormatService.findByName(FileFormatService.TETRAD_TABULAR);

        return (fileFormat == null) ? 0 : fileFormat.getId();
    }

    public Map<String, List<FileFormat>> getFileFormatGroupOpts() {
        Map<String, List<FileFormat>> map = new TreeMap<>();

        FileType fileType = fileTypeService.findByName(FileTypeService.RESULT);
        List<FileFormat> fileFormats = fileFormatService.findByFileTypeNot(fileType);
        fileFormats.forEach(fileFormat -> {
            String key = fileFormat.getFileType().getDisplayName();
            List<FileFormat> list = map.get(key);
            if (list == null) {
                list = new LinkedList<>();
                map.put(key, list);
            }
            list.add(fileFormat);
        });

        return map;
    }

    public FileCategorizeForm toFileCategorizeForm(File file) {
        FileCategorizeForm form = getdefaultForm();

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat != null) {
            form.setFileFormatId(fileFormat.getId());

            if (FileFormatService.TETRAD_TABULAR.equals(fileFormat.getName())) {
                TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
                if (dataFile != null) {
                    form.setFileDelimiterId(dataFile.getFileDelimiterType().getId());
                    form.setVariableTypeId(dataFile.getFileVariableType().getId());
                    form.setQuoteChar(dataFile.getQuoteChar());
                    form.setMissingValueMarker(dataFile.getMissingValueMarker());
                    form.setCommentMarker(dataFile.getCommentMarker());
                }
            }
        }

        return form;
    }

    public FileCategorizeForm getdefaultForm() {
        FileCategorizeForm form = new FileCategorizeForm();

        List<FileFormat> fileFormats = fileFormatService.findAll();
        if (!fileFormats.isEmpty()) {
            form.setFileFormatId(fileFormats.get(0).getId());
        }

        List<FileDelimiterType> delimiterTypes = fileDelimiterTypeService.findAll();
        if (!delimiterTypes.isEmpty()) {
            form.setFileDelimiterId(delimiterTypes.get(0).getId());
        }

        List<FileVariableType> variableTypes = fileVariableTypeService.findAll();
        if (!variableTypes.isEmpty()) {
            form.setVariableTypeId(variableTypes.get(0).getId());
        }

        form.setQuoteChar('"');
        form.setMissingValueMarker("*");
        form.setCommentMarker("#");

        return form;
    }

    public List<AttrValue> getAdditionalInfo(File file) {
        List<AttrValue> attrValues = new LinkedList<>();

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat != null) {
            switch (fileFormat.getName()) {
                case FileFormatService.TETRAD_TABULAR:
                    TetradDataFile dataFile = tetradDataFileService.getRepository().findByFile(file);
                    attrValues.add(new AttrValue("Number of Columns", String.valueOf(dataFile.getNumOfColumns())));
                    attrValues.add(new AttrValue("Number of Rows", String.valueOf(dataFile.getNumOfRows())));
                    attrValues.add(new AttrValue("Delimiter", dataFile.getFileDelimiterType().getDisplayName()));
                    attrValues.add(new AttrValue("Variable Type", dataFile.getFileVariableType().getDisplayName()));
                    attrValues.add(new AttrValue("Quote Character", String.valueOf(dataFile.getQuoteChar())));
                    attrValues.add(new AttrValue("Missing Value Marker", dataFile.getMissingValueMarker()));
                    attrValues.add(new AttrValue("Comment Marker", dataFile.getCommentMarker()));
                    break;
                case FileFormatService.TETRAD_VARIABLE:
                    TetradVariableFile varFile = tetradVariableFileService.getRepository().findByFile(file);
                    attrValues.add(new AttrValue("Number of Variables", String.valueOf(varFile.getNumOfVariables())));
                    break;
            }
        }

        return attrValues;
    }

}
