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
import edu.pitt.dbmi.ccd.web.model.data.DataListItem;
import edu.pitt.dbmi.ccd.web.model.data.DataSummary;
import edu.pitt.dbmi.ccd.web.service.data.RemoteDataFileService;
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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private final RemoteDataFileService remoteDataFileService;

    @Autowired(required = true)
    public DataService(
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            UserAccountService userAccountService,
            RemoteDataFileService remoteDataFileService) {
        this.dataFileService = dataFileService;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.userAccountService = userAccountService;
        this.remoteDataFileService = remoteDataFileService;
    }

    public List<AttributeValue> getFileInfo(final String dir, final String fileName) {
        List<AttributeValue> list = new LinkedList<>();

        Path file = Paths.get(dir, fileName);
        try {
            BasicFileInfo info = FileInfos.basicPathInfo(file);
            list.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            list.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            list.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            list.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(dir, fileName);
        if (dataFile != null) {
            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo != null) {
                list.add(new AttributeValue("MD5:", dataFileInfo.getMd5checkSum()));
            }
        }

        return list;
    }

    public Map<String, String> listAlgoDataset(String prefix, String username, VariableType variableType) {
        Map<String, String> map = new TreeMap<>();

        if (variableType != null) {
            UserAccount userAccount = userAccountService.findByUsername(username);

            Pattern p = Pattern.compile(prefix);

            //get the files stored in the database
            Map<String, CombinedFileInfo> combinedFiles = new HashMap<>();
            List<DataFile> dbDataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
            dbDataFiles.forEach(dataFile -> {
                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
                if (dataFileInfo != null) {
                    VariableType varType = dataFileInfo.getVariableType();
                    if (varType != null && varType.getId().equals(variableType.getId())) {
                        String name = dataFile.getName();
                        Matcher m = p.matcher(name);
                        if (m.find()) {
                            String fileName = m.group(0);
                            long size = dataFile.getFileSize();
                            CombinedFileInfo info = combinedFiles.get(fileName);
                            if (info == null) {
                                combinedFiles.put(fileName, new CombinedFileInfo(size, 1));
                            } else {
                                info.count++;
                                info.size += size;
                            }
                        }
                    }
                }
            });

            Set<String> keySet = combinedFiles.keySet();
            keySet.forEach(key -> {
                CombinedFileInfo info = combinedFiles.get(key);
                String size = FilePrint.humanReadableSize(info.size, true);
                String description = String.format("%s (count: %d, total size: %s)", key, info.count, size);
                map.put(key, description);
            });
        }

        return map;
    }

    public Map<String, String> listAlgoDataset(String username, VariableType variableType) {
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

    public List<DataListItem> createListItem(String username, String dataDir, VariableType variableType) {
        String varType = "continuous";
        List<DataListItem> listItems = createListItem(username, dataDir);

        return listItems.stream()
                .filter(item -> varType.equals(item.getVariableType()))
                .collect(Collectors.toList());
    }

    public List<DataListItem> createListItem(String username, String dataDir) {
        List<DataListItem> listItems = new LinkedList<>();

        Set<String> remoteFileHashes = remoteDataFileService.retrieveDataFileMD5Hash(username);

        UserAccount userAccount = userAccountService.findByUsername(username);

        List<DataFile> dataFiles = dataFileService.findByUserAccounts(Collections.singleton(userAccount));
        Map<String, DataFile> dbDataFile = new HashMap<>();
        dataFiles.forEach(file -> {
            dbDataFile.put(file.getName(), file);
        });

        Map<String, DataFile> localDataFile = new HashMap<>();
        try {
            List<Path> list = FileInfos.listDirectory(Paths.get(dataDir), false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
            for (Path file : files) {
                BasicFileInfo info = FileInfos.basicPathInfo(file);

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
                    dataFile.setUserAccounts(Collections.singleton(userAccount));

                    localDataFile.put(fileName, dataFile);
                }

                DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
                if (dataFileInfo == null) {
                    dataFileInfo = new DataFileInfo();
                    dataFileInfo.setMd5checkSum(MessageDigestHash.computeMD5Hash(file));
                    dataFile.setDataFileInfo(dataFileInfo);

                    localDataFile.put(fileName, dataFile);
                } else {
                    FileDelimiter delimiter = dataFileInfo.getFileDelimiter();
                    if (delimiter != null) {
                        item.setDelimiter(delimiter.getName());
                    }

                    VariableType variableType = dataFileInfo.getVariableType();
                    if (variableType != null) {
                        item.setVariableType(variableType.getName());
                    }
                }

                if (remoteFileHashes.contains(dataFileInfo.getMd5checkSum())) {
                    item.setOnCloud(true);
                }

                dbDataFile.remove(fileName);

                listItems.add(item);
            }
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
        if (!localDataFile.isEmpty()) {
            List<DataFile> list = new LinkedList<>();
            Set<String> keySet = localDataFile.keySet();
            keySet.forEach(key -> {
                list.add(localDataFile.get(key));
            });
            dataFileService.saveDataFile(list);
        }

        return listItems;
    }

    public List<AttributeValue> getDataFileAdditionalInfo(String absolutePath, String name) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo != null) {
            Integer numOfRows = dataFileInfo.getNumOfRows();
            Integer numOfCols = dataFileInfo.getNumOfColumns();
            String md5CheckSum = dataFileInfo.getMd5checkSum();
            fileInfo.add(new AttributeValue("Row(s):", (numOfRows == null) ? "" : numOfRows.toString()));
            fileInfo.add(new AttributeValue("Column(s):", (numOfCols == null) ? "" : numOfCols.toString()));
            fileInfo.add(new AttributeValue("MD5 Checksum:", (md5CheckSum == null) ? "" : md5CheckSum));
//            fileInfo.add(new AttributeValue("Missing Value:", dataFileInfo.getMissingValue() ? "Yes" : "No"));
        }

        return fileInfo;
    }

    public List<AttributeValue> getDataFileAdditionalInfo(String absolutePath, String name, DataSummary dataSummary) {
        List<AttributeValue> fileInfo = new LinkedList<>();

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);

        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo == null) {
            dataFileInfo = new DataFileInfo();
        }
        dataFileInfo.setFileDelimiter(dataSummary.getFileDelimiter());
        dataFileInfo.setVariableType(dataSummary.getVariableType());

        try {
            char delimiter = FileInfos.delimiterNameToChar(dataSummary.getFileDelimiter().getName());
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

        fileInfo.add(new AttributeValue("Row(s):", String.valueOf(dataFileInfo.getNumOfRows())));
        fileInfo.add(new AttributeValue("Column(s):", String.valueOf(dataFileInfo.getNumOfColumns())));
        fileInfo.add(new AttributeValue("MD5 Checksum:", dataFileInfo.getMd5checkSum()));
//        fileInfo.add(new AttributeValue("Missing Value:", dataFileInfo.getMissingValue() ? "Yes" : "No"));

        return fileInfo;
    }

    public DataSummary getDataSummary(String absolutePath, String name) {
        DataSummary dataSummary = new DataSummary();
        dataSummary.setFileName(name);

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);
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

        return dataSummary;
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

    private class CombinedFileInfo {

        long size;
        int count;

        public CombinedFileInfo(long size, int count) {
            this.size = size;
            this.count = count;
        }

    }

}
