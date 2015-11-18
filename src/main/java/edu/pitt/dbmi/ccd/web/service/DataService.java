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
import edu.pitt.dbmi.ccd.commons.file.MessageDigestHash;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiter;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.model.data.DataSummary;
import edu.pitt.dbmi.ccd.web.model.file.DatasetFileInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Nov 13, 2015 10:09:40 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    final String workspace;

    final String dataFolder;

    private final DataFileService dataFileService;

    private final VariableTypeService variableTypeService;

    private final FileDelimiterService fileDelimiterService;

    private final UserAccountService userAccountService;

    @Autowired
    public DataService(
            @Value("${ccd.server.workspace}") String workspace,
            @Value("${ccd.folder.data:data}") String dataFolder,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            UserAccountService userAccountService) {
        this.workspace = workspace;
        this.dataFolder = dataFolder;
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.userAccountService = userAccountService;
    }

    public String getFileDelimiter(String fileName, String username) {
        String delimiter = null;

        Path dataDir = Paths.get(workspace, username, dataFolder);
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(dataDir.toAbsolutePath().toString(), fileName);
        if (dataFile != null) {
            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo != null) {
                FileDelimiter fileDelimiter = dataFileInfo.getFileDelimiter();
                if (fileDelimiter != null) {
                    delimiter = fileDelimiter.getValue();
                }
            }
        }

        return delimiter;
    }

    public List<VariableType> getVariableTypes() {
        return variableTypeService.findAll();
    }

    public List<FileDelimiter> getFileDelimiters() {
        return fileDelimiterService.findAll();
    }

    public Map<String, String> listAlgorithmDataset(String username, VariableType variableType) {
        Map<String, String> map = new TreeMap<>();

        if (variableType != null) {
            UserAccount userAccount = userAccountService.findByUsername(username);

            //get the files stored in the database
            List<DataFile> dbDataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
            dbDataFiles.forEach(dataFile -> {
                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
                if (dataFileInfo != null) {
                    VariableType varType = dataFileInfo.getVariableType();
                    if (varType != null && varType.getId().equals(variableType.getId())) {
                        String size = FilePrint.humanReadableSize(dataFile.getFileSize(), true);
                        String name = dataFile.getName();
                        String description = String.format("%s (%s)", name, size);

                        map.put(name, description);
                    }
                }
            });
        }

        return map;
    }

    public boolean saveDataSummary(final DataSummary dataSummary, final String username) {
        boolean success = false;

        Path dataDir = Paths.get(workspace, username, dataFolder);
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(dataDir.toAbsolutePath().toString(), dataSummary.getFileName());
        if (dataFile != null) {
            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo == null) {
                dataFileInfo = new DataFileInfo();
            }
            dataFileInfo.setFileDelimiter(dataSummary.getFileDelimiter());
            dataFileInfo.setVariableType(dataSummary.getVariableType());

            try {
                char delimiter = FileInfos.delimiterNameToChar(dataSummary.getFileDelimiter().getName());
                Path file = Paths.get(workspace, username, dataFolder, dataSummary.getFileName());
                dataFileInfo.setNumOfRows(FileInfos.countLine(file.toFile()));
                dataFileInfo.setNumOfColumns(FileInfos.countColumn(file.toFile(), delimiter));
                dataFileInfo.setMd5checkSum(MessageDigestHash.computeMD5Hash(file));
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }

            dataFileInfo.setMissingValue(Boolean.FALSE);

            dataFile.setDataFileInfo(dataFileInfo);
            dataFileService.saveDataFile(dataFile);

            success = true;
        }

        return success;
    }

    public DataSummary getDataSummary(final String fileName, final String username) {
        DataSummary dataSummary = new DataSummary();

        Path dataDir = Paths.get(workspace, username, dataFolder);
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(dataDir.toAbsolutePath().toString(), fileName);
        if (dataFile == null) {
            dataSummary.setFileName("");
            dataSummary.setFileDelimiter(new FileDelimiter("", ""));
            dataSummary.setVariableType(new VariableType(""));
        } else {
            dataSummary.setFileName(fileName);

            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo == null) {
                dataSummary.setVariableType(variableTypeService.findByName("continuous"));
                dataSummary.setFileDelimiter(fileDelimiterService.getFileDelimiterRepository().findByName("tab"));
            } else {
                VariableType variableType = dataFileInfo.getVariableType();
                if (variableType == null) {
                    variableType = variableTypeService.findByName("continuous");
                }
                dataSummary.setVariableType(variableType);

                FileDelimiter delimiter = dataFileInfo.getFileDelimiter();
                if (delimiter == null) {
                    delimiter = fileDelimiterService.getFileDelimiterRepository().findByName("tab");
                }
                dataSummary.setFileDelimiter(delimiter);
            }
        }

        return dataSummary;
    }

    public boolean deleteDataFile(final String fileName, final String username) {
        boolean success = false;

        Path dataDir = Paths.get(workspace, username, dataFolder);
        if (dataFileService.deleteDataFileByNameAndAbsolutePath(dataDir.toAbsolutePath().toString(), fileName)) {
            Path dataFile = Paths.get(workspace, username, dataFolder, fileName);
            try {
                Files.deleteIfExists(dataFile);
                success = true;
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        return success;
    }

    public List<AttributeValue> getDataFileAdditionalInfo(final String fileName, final String username) {
        List<AttributeValue> list = new LinkedList<>();

        Path dataDir = Paths.get(workspace, username, dataFolder);
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(dataDir.toAbsolutePath().toString(), fileName);
        if (dataFile != null) {
            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo != null) {
                Integer numOfRows = dataFileInfo.getNumOfRows();
                Integer numOfCols = dataFileInfo.getNumOfColumns();
                FileDelimiter delimiter = dataFileInfo.getFileDelimiter();
                VariableType variableType = dataFileInfo.getVariableType();
                list.add(new AttributeValue("Row(s):", (numOfRows == null) ? "" : numOfRows.toString()));
                list.add(new AttributeValue("Column(s):", (numOfCols == null) ? "" : numOfCols.toString()));
                list.add(new AttributeValue("Delimiter:", (delimiter == null) ? "" : delimiter.getName()));
                list.add(new AttributeValue("Variable Type:", (variableType == null) ? "" : variableType.getName()));
            }
        }

        return list;
    }

    public List<AttributeValue> getFileInfo(final String fileName, final String username) {
        List<AttributeValue> list = new LinkedList<>();

        Path data = Paths.get(workspace, username, dataFolder, fileName);
        try {
            BasicFileInfo info = FileInfos.basicPathInfo(data);
            list.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            list.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            list.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            list.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        Path dataDir = Paths.get(workspace, username, dataFolder);
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(dataDir.toAbsolutePath().toString(), fileName);
        if (dataFile != null) {
            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo != null) {
                list.add(new AttributeValue("MD5:", dataFileInfo.getMd5checkSum()));
            }
        }

        return list;
    }

    public List<DatasetFileInfo> listDataFiles(final String username) {
        List<DatasetFileInfo> datasetFileInfos = new LinkedList<>();

        refreshLocalFileDatabase(Paths.get(workspace, username, dataFolder), username);

        UserAccount userAccount = userAccountService.findByUsername(username);
        List<DataFile> dataFileList = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        DataFile[] dataFiles = dataFileList.toArray(new DataFile[dataFileList.size()]);
        Arrays.sort(dataFiles, (dataFile1, dataFile2) -> {
            return dataFile2.getCreationTime().compareTo(dataFile1.getCreationTime());
        });

        for (DataFile dataFile : dataFiles) {
            DatasetFileInfo datasetFileInfo = new DatasetFileInfo();
            datasetFileInfo.setCreationDate(dataFile.getCreationTime());
            datasetFileInfo.setFileName(dataFile.getName());
            datasetFileInfo.setFileSize(dataFile.getFileSize());

            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            datasetFileInfo.setSummarized(dataFileInfo.getFileDelimiter() != null);

            datasetFileInfos.add(datasetFileInfo);
        }

        return datasetFileInfos;
    }

    public int countFiles(final String username) {
        int count = 0;

        try {
            Path dir = Paths.get(workspace, username, dataFolder);
            List<Path> list = FileInfos.listDirectory(dir, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
            count = files.size();
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return count;
    }

    public void refreshLocalFileDatabase(final Path dataDir, final String username) {
        UserAccount userAccount = userAccountService.findByUsername(username);

        // get all the user's dataset from the database
        List<DataFile> dataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        Map<String, DataFile> dbDataFile = new HashMap<>();
        dataFiles.forEach(file -> {
            dbDataFile.put(file.getName(), file);
        });

        Map<String, DataFile> saveFiles = new HashMap<>();
        try {
            List<Path> localFiles = FileInfos.listDirectory(dataDir, false);
            localFiles.forEach(localFile -> {
                String fileName = localFile.getFileName().toString();

                DataFile dataFile = dbDataFile.get(fileName);
                if (dataFile == null) {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(localFile, BasicFileAttributes.class);
                        long creationTime = attrs.creationTime().toMillis();
                        long lastModifiedTime = attrs.lastModifiedTime().toMillis();
                        long fileSize = attrs.size();

                        dataFile = new DataFile();
                        dataFile.setName(fileName);
                        dataFile.setAbsolutePath(localFile.getParent().toRealPath().toString());
                        dataFile.setCreationTime(new Date(creationTime));
                        dataFile.setFileSize(fileSize);
                        dataFile.setLastModifiedTime(new Date(lastModifiedTime));
                        dataFile.setUserAccounts(Collections.singleton(userAccount));

                        DataFileInfo dataFileInfo = new DataFileInfo();
                        dataFileInfo.setMd5checkSum(MessageDigestHash.computeMD5Hash(Paths.get(dataFile.getAbsolutePath(), dataFile.getName())));
                        dataFile.setDataFileInfo(dataFileInfo);

                        saveFiles.put(fileName, dataFile);
                    } catch (IOException exception) {
                        LOGGER.error(exception.getMessage());
                    }
                } else {
                    DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
                    if (dataFileInfo == null) {
                        try {
                            dataFileInfo = new DataFileInfo();
                            dataFileInfo.setMd5checkSum(MessageDigestHash.computeMD5Hash(localFile));
                            dataFile.setDataFileInfo(dataFileInfo);

                            saveFiles.put(fileName, dataFile);
                            dbDataFile.remove(fileName);
                        } catch (IOException exception) {
                            LOGGER.error(exception.getMessage());
                        }
                    } else {
                        dbDataFile.remove(fileName);
                    }
                }
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        if (!dbDataFile.isEmpty()) {
            List<DataFile> list = new LinkedList<>();
            Set<String> keySet = dbDataFile.keySet();
            keySet.forEach(key -> {
                list.add(dbDataFile.get(key));
            });
            dataFileService.deleteDataFile(list);
        }

        // save all the new files found in the workspace
        if (!saveFiles.isEmpty()) {
            List<DataFile> list = new LinkedList<>();
            Set<String> keySet = saveFiles.keySet();
            keySet.forEach(key -> {
                list.add(saveFiles.get(key));
            });
            dataFileService.saveDataFile(list);
        }
    }

}
