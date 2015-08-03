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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.model.DataListItem;
import edu.pitt.dbmi.ccd.web.model.DataValidation;
import edu.pitt.dbmi.ccd.web.util.MessageDigestHash;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 24, 2015 11:04:28 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    private final DataFileService dataFileService;

    private final VariableTypeService variableTypeService;

    private final FileDelimiterService fileDelimiterService;

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public DataService(
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            UserAccountService userAccountService) {
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.userAccountService = userAccountService;
    }

    public List<DataFile> listDirectorySync(String dataDir, String username, VariableType variableType) {
        if (variableType == null) {
            return new LinkedList<>();
        }

        List<DataFile> results = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);

        //get the files stored in the database
        List<DataFile> dbDataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        Map<String, DataFile> dbDataFileMap = new HashMap<>();
        dbDataFiles.forEach(file -> {
            dbDataFileMap.put(file.getName(), file);
        });

        List<DataFile> dataFileToSave = new LinkedList<>();
        try {
            List<Path> localFileAndDir = FileInfos.listDirectory(Paths.get(dataDir), false);
            List<Path> localFiles = localFileAndDir.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<BasicFileInfo> localFileInfos = FileInfos.listBasicPathInfo(localFiles);
            localFileInfos.forEach(info -> {
                String fileName = info.getFilename();

                DataFile dataFile = dbDataFileMap.get(fileName);
                if (dataFile == null) {
                    dataFile = new DataFile();
                    dataFile.setName(fileName);
                    dataFile.setAbsolutePath(info.getAbsolutePath().toString());
                    dataFile.setCreationTime(new Date(info.getCreationTime()));
                    dataFile.setFileSize(info.getSize());
                    dataFile.setLastModifiedTime(new Date(info.getLastModifiedTime()));
                    dataFile.setDataFileInfo(null);
                    dataFile.setUserAccounts(Collections.singleton(userAccount));

                    dataFileToSave.add(dataFile);
                } else {
                    dbDataFileMap.remove(fileName);
                }

                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
                if (dataFileInfo != null) {
                    VariableType varType = dataFileInfo.getVariableType();
                    if (varType != null && varType.getId().equals(variableType.getId())) {
                        results.add(dataFile);
                    }
                }
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        // save all the new files found in the workspace
        if (!dataFileToSave.isEmpty()) {
            dataFileService.saveDataFile(dataFileToSave);
        }

        List<DataFile> dataFileToRemove = new LinkedList<>();
        Set<String> keySet = dbDataFileMap.keySet();
        keySet.forEach(key -> {
            dataFileToRemove.add(dbDataFileMap.get(key));
        });
        if (!dataFileToRemove.isEmpty()) {
            dataFileService.deleteDataFile(dataFileToRemove);
        }

        return results;
    }

    /**
     * Read the retrieve the data files from the user's local directory and sync
     * with the list in the database.
     *
     * @param username application username
     * @param dataDir directory contain user's data file
     * @return
     */
    public List<DataFile> listDirectorySync(String dataDir, String username) {
        List<DataFile> results = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);

        //get the files stored in the database
        List<DataFile> dbDataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        Map<String, DataFile> dbDataFileMap = new HashMap<>();
        dbDataFiles.forEach(file -> {
            dbDataFileMap.put(file.getName(), file);
        });

        List<DataFile> dataFileToSave = new LinkedList<>();
        try {
            List<Path> localFileAndDir = FileInfos.listDirectory(Paths.get(dataDir), false);
            List<Path> localFiles = localFileAndDir.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<BasicFileInfo> localFileInfos = FileInfos.listBasicPathInfo(localFiles);
            localFileInfos.forEach(info -> {
                String fileName = info.getFilename();

                DataFile dataFile = dbDataFileMap.get(fileName);
                if (dataFile == null) {
                    dataFile = new DataFile();
                    dataFile.setName(fileName);
                    dataFile.setAbsolutePath(info.getAbsolutePath().toString());
                    dataFile.setCreationTime(new Date(info.getCreationTime()));
                    dataFile.setFileSize(info.getSize());
                    dataFile.setLastModifiedTime(new Date(info.getLastModifiedTime()));
                    dataFile.setDataFileInfo(null);
                    dataFile.setUserAccounts(Collections.singleton(userAccount));

                    dataFileToSave.add(dataFile);
                } else {
                    dbDataFileMap.remove(fileName);
                }

                results.add(dataFile);
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        // save all the new files found in the workspace
        if (!dataFileToSave.isEmpty()) {
            dataFileService.saveDataFile(dataFileToSave);
        }

        List<DataFile> dataFileToRemove = new LinkedList<>();
        Set<String> keySet = dbDataFileMap.keySet();
        keySet.forEach(key -> {
            dataFileToRemove.add(dbDataFileMap.get(key));
        });
        if (!dataFileToRemove.isEmpty()) {
            dataFileService.deleteDataFile(dataFileToRemove);
        }

        return results;
    }

    public List<DataListItem> createListItem(String username, String dataDir, VariableType variableType) {
        String varType = "continuous";
        List<DataListItem> listItems = createListItem(username, dataDir);

        return listItems.stream()
                .filter(item -> varType.equals(item.getVariableType()))
                .collect(Collectors.toList());
    }

    public List<DataListItem> createListItem(String username, String dataDir) {
        List<DataListItem> listItems = new LinkedList<>();

        UserAccount userAccount = userAccountService.findByUsername(username);

        List<DataFile> dataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        Map<String, DataFile> dbDataFile = new HashMap<>();
        dataFiles.forEach(file -> {
            dbDataFile.put(file.getName(), file);
        });

        List<DataFile> dataFileToSave = new LinkedList<>();
        List<DataFile> dataFileToRemove = new LinkedList<>();
        try {
            List<Path> list = FileInfos.listDirectory(Paths.get(dataDir), false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<BasicFileInfo> result = FileInfos.listBasicPathInfo(files);
            result.forEach(info -> {
                String fileName = info.getFilename();
                String creationDate = FilePrint.fileTimestamp(info.getCreationTime());
                String size = FilePrint.humanReadableSize(info.getSize(), true);

                DataListItem item = new DataListItem(fileName, creationDate, size);

                DataFile dataFile = dbDataFile.get(fileName);
                if (dataFile == null) {
                    dataFile = new DataFile();
                    dataFile.setName(fileName);
                    dataFile.setAbsolutePath(info.getAbsolutePath().toString());
                    dataFile.setCreationTime(new Date(info.getCreationTime()));
                    dataFile.setFileSize(info.getSize());
                    dataFile.setLastModifiedTime(new Date(info.getLastModifiedTime()));
                    dataFile.setDataFileInfo(null);
                    dataFile.setUserAccounts(Collections.singleton(userAccount));

                    dataFileToSave.add(dataFile);
                }

                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
                if (dataFileInfo != null) {
                    item.setDelimiter(dataFileInfo.getFileDelimiter().getName());
                    item.setVariableType(dataFileInfo.getVariableType().getName());
                }

                dbDataFile.remove(fileName);

                listItems.add(item);
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        Set<String> keySet = dbDataFile.keySet();
        keySet.forEach(key -> {
            dataFileToRemove.add(dbDataFile.get(key));
        });
        if (!dataFileToRemove.isEmpty()) {
            dataFileService.deleteDataFile(dataFileToRemove);
        }

        // save all the new files found in the workspace
        if (!dataFileToSave.isEmpty()) {
            dataFileService.saveDataFile(dataFileToSave);
        }

        return listItems;
    }

    public List<AttributeValue> getDataFileAdditionalInfo(String absolutePath, String name) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo != null) {
            fileInfo.add(new AttributeValue("Row:", String.valueOf(dataFileInfo.getNumOfRows())));
            fileInfo.add(new AttributeValue("Column:", String.valueOf(dataFileInfo.getNumOfColumns())));
            fileInfo.add(new AttributeValue("MD5:", dataFileInfo.getMd5checkSum()));
//            fileInfo.add(new AttributeValue("Missing Value:", dataFileInfo.getMissingValue() ? "Yes" : "No"));
        }

        return fileInfo;
    }

    public List<AttributeValue> getDataFileAdditionalInfo(String absolutePath, String name, DataValidation dataValidation) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo == null) {
            dataFileInfo = new DataFileInfo();
        }
        dataFileInfo.setFileDelimiter(dataValidation.getFileDelimiter());
        dataFileInfo.setVariableType(dataValidation.getVariableType());

        try {
            char delimiter = FileInfos.delimiterNameToChar(dataValidation.getFileDelimiter().getName());
            Path file = Paths.get(absolutePath, name);
            dataFileInfo.setNumOfRows(FileInfos.countLine(file.toFile()));
            dataFileInfo.setNumOfColumns(FileInfos.countColumn(file.toFile(), delimiter));
            dataFileInfo.setMd5checkSum(MessageDigestHash.computeMD5Hash(file));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        dataFileInfo.setMissingValue(Boolean.FALSE);

        dataFile.setDataFileInfo(dataFileInfo);
        dataFileService.saveDataFile(dataFile);

        fileInfo.add(new AttributeValue("Row:", String.valueOf(dataFileInfo.getNumOfRows())));
        fileInfo.add(new AttributeValue("Column:", String.valueOf(dataFileInfo.getNumOfColumns())));
        fileInfo.add(new AttributeValue("MD5:", dataFileInfo.getMd5checkSum()));
//        fileInfo.add(new AttributeValue("Missing Value:", dataFileInfo.getMissingValue() ? "Yes" : "No"));

        return fileInfo;
    }

    public DataValidation getDataValidation(String absolutePath, String name) {
        DataValidation dataValidation = new DataValidation();
        dataValidation.setFileName(name);

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo == null) {
            dataValidation.setVariableType(variableTypeService.findByName("continuous"));
            dataValidation.setFileDelimiter(fileDelimiterService.getFileDelimiterRepository()
                    .findByName("tab"));
        } else {
            dataValidation.setVariableType(dataFileInfo.getVariableType());
            dataValidation.setFileDelimiter(dataFileInfo.getFileDelimiter());
        }

        return dataValidation;
    }

    public DataFileService getDataFileService() {
        return dataFileService;
    }

    public VariableTypeService getVariableTypeService() {
        return variableTypeService;
    }

    public FileDelimiterService getFileDelimiterService() {
        return fileDelimiterService;
    }

}
