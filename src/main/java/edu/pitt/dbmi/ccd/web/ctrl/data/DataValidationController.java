/*
 * Copyright (C) 2015 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.ctrl.data;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.repository.DataFileInfoRepository;
import edu.pitt.dbmi.ccd.db.repository.DataFileRepository;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.model.DataValidation;
import edu.pitt.dbmi.ccd.web.service.DataFileInfoService;
import edu.pitt.dbmi.ccd.web.service.DataFileService;
import edu.pitt.dbmi.ccd.web.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.web.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.util.MessageDigestHash;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 21, 2015 12:34:26 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/data/validation")
public class DataValidationController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataValidationController.class);

    private final VariableTypeService variableTypeService;

    private final FileDelimiterService fileDelimiterService;

    private final DataFileInfoService dataFileInfoService;

    private final DataFileService dataFileService;

    @Autowired(required = true)
    public DataValidationController(
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            DataFileInfoService dataFileInfoService,
            DataFileService dataFileService) {
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.dataFileInfoService = dataFileInfoService;
        this.dataFileService = dataFileService;
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public String runDataValidation(
            @ModelAttribute("dataValidation") DataValidation dataValidation,
            @ModelAttribute("appUser") AppUser appUser,
            Model model) {
        String fileName = dataValidation.getFileName();
        Path file = Paths.get(appUser.getUploadDirectory(), fileName);

        model.addAttribute("fileName", fileName);
        model.addAttribute("basicInfo", getFileBasicInfo(file));
        model.addAttribute("additionalInfo", getFileAdditionalInfo(file, dataValidation));
        model.addAttribute("dataValidation", getDataValidation(fileName));
        model.addAttribute("variableTypes", variableTypeService.findAll());
        model.addAttribute("fileDelimiters", fileDelimiterService.findAll());

        return DATA_VALIDATION;
    }

    @RequestMapping(value = "/validate", method = RequestMethod.GET)
    public String showDataValidation(
            @RequestParam(value = "file") String fileName,
            @ModelAttribute("appUser") AppUser appUser,
            Model model) throws IOException {
        Path file = Paths.get(appUser.getUploadDirectory(), fileName);

        model.addAttribute("fileName", fileName);
        model.addAttribute("basicInfo", getFileBasicInfo(file));
        model.addAttribute("additionalInfo", getFileAdditionalInfo(file));
        model.addAttribute("dataValidation", getDataValidation(fileName));
        model.addAttribute("variableTypes", variableTypeService.findAll());
        model.addAttribute("fileDelimiters", fileDelimiterService.findAll());

        return DATA_VALIDATION;
    }

    private List<AttributeValue> getFileAdditionalInfo(Path file, DataValidation dataValidation) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        DataFileInfoRepository dataFileInfoRepository = dataFileInfoService.getDataFileInfoRepository();
        DataFileRepository dataFileRepository = dataFileService.getDataFileRepository();

        String fileName = file.getFileName().toString();
        DataFile dataFile = dataFileRepository.findByName(fileName);

        DataFileInfo dataFileInfo = dataFileInfoRepository.findByDataFile(dataFile);
        if (dataFileInfo == null) {
            dataFileInfo = new DataFileInfo(dataFile);
        }
        dataFileInfo.setFileDelimiter(dataValidation.getFileDelimiter());
        dataFileInfo.setVariableType(dataValidation.getVariableType());

        char delimiter = FileInfos.delimiterStringToChar(dataValidation.getFileDelimiter().getName());
        try {
            dataFileInfo.setNumOfRows(FileInfos.countLine(file.toFile()));
            dataFileInfo.setNumOfColumns(FileInfos.countColumn(file.toFile(), delimiter));
            dataFileInfo.setMd5checkSum(MessageDigestHash.computeMD5Hash(file));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        dataFileInfo.setMissingValue(Boolean.FALSE);
        dataFileInfoRepository.save(dataFileInfo);

        fileInfo.add(new AttributeValue("Row:", String.valueOf(dataFileInfo.getNumOfRows())));
        fileInfo.add(new AttributeValue("Column:", String.valueOf(dataFileInfo.getNumOfColumns())));
        fileInfo.add(new AttributeValue("MD5:", dataFileInfo.getMd5checkSum()));
        fileInfo.add(new AttributeValue("Missing Value:", dataFileInfo.getMissingValue() ? "Yes" : "No"));

        return fileInfo;
    }

    private List<AttributeValue> getFileAdditionalInfo(Path file) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        String fileName = file.getFileName().toString();
        DataFileInfo dataFileInfo = dataFileInfoService.getDataFileInfoRepository().findByDataFileName(fileName);
        if (dataFileInfo != null) {
            fileInfo.add(new AttributeValue("Row:", String.valueOf(dataFileInfo.getNumOfRows())));
            fileInfo.add(new AttributeValue("Column:", String.valueOf(dataFileInfo.getNumOfColumns())));
            fileInfo.add(new AttributeValue("MD5:", dataFileInfo.getMd5checkSum()));
            fileInfo.add(new AttributeValue("Missing Value:", dataFileInfo.getMissingValue() ? "Yes" : "No"));
//            fileInfo.add(new AttributeValue());
//            fileInfo.add(new AttributeValue());
        }
        return fileInfo;
    }

    private List<AttributeValue> getFileBasicInfo(Path file) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        try {
            BasicFileInfo info = FileInfos.basicPathInfo(file);
            fileInfo.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            fileInfo.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            fileInfo.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            fileInfo.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return fileInfo;
    }

    private DataValidation getDataValidation(String fileName) {
        DataValidation dataValidation = new DataValidation();

        dataValidation.setFileName(fileName);
        dataValidation.setVariableType(variableTypeService.getVariableTypeRepository()
                .findByName("continuous"));
        dataValidation.setFileDelimiter(fileDelimiterService.getFileDelimiterRepository()
                .findByName("tab"));

        return dataValidation;
    }

}
