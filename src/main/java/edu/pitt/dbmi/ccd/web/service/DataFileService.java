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
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.repository.DataFileInfoRepository;
import edu.pitt.dbmi.ccd.db.repository.DataFileRepository;
import edu.pitt.dbmi.ccd.web.model.DataListItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Jul 21, 2015 9:05:34 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
@Transactional
public class DataFileService {

    private final DataFileRepository dataFileRepository;

    private final DataFileInfoRepository dataFileInfoRepository;

    @Autowired(required = true)
    public DataFileService(
            DataFileRepository dataFileRepository,
            DataFileInfoRepository dataFileInfoRepository) {
        this.dataFileRepository = dataFileRepository;
        this.dataFileInfoRepository = dataFileInfoRepository;
    }

    public boolean deleteDataFileByNameAndAbsolutePath(String name, String absolutePath) {
        try {
            DataFile dataFile = dataFileRepository.findByNameAndAbsolutePath(name, absolutePath);
            dataFileInfoRepository.deleteByDataFile(dataFile);
            dataFileRepository.delete(dataFile);
        } catch (Exception exception) {
            return false;
        }

        return true;
    }

    public boolean deleteDataFile(DataFile dataFile) {
        try {
            dataFileInfoRepository.deleteByDataFile(dataFile);
            dataFileRepository.delete(dataFile);
        } catch (Exception exception) {
            return false;
        }

        return true;
    }

    public List<DataListItem> createListItem(String baseDir, VariableType variableType) {
        List<DataListItem> results = new LinkedList<>();

        List<DataListItem> listItems = createListItem(baseDir);
        listItems.forEach(item -> {
            DataFileInfo dataFileInfo = dataFileInfoRepository
                    .findByDataFileNameAndAbsolutePath(item.getFileName(), baseDir);
            if (dataFileInfo != null) {
                VariableType varType = dataFileInfo.getVariableType();
                if (varType.getId().longValue() == variableType.getId()) {
                    results.add(item);
                }
            }
        });

        return results;
    }

    public List<DataListItem> createListItem(String baseDir) {
        List<DataListItem> listItems = new LinkedList<>();

        Path basePath = Paths.get(baseDir);
        List<DataFile> dataFileToSave = new LinkedList<>();
        try {
            List<Path> list = FileInfos.listDirectory(basePath, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            // get the filenames of the data that are stored in the database
            Set<String> dbFileName = new HashSet<>();
            List<DataFile> dataFiles = dataFileRepository.findByAbsolutePath(baseDir);
            dataFiles.forEach(info -> {
                dbFileName.add(info.getName());
            });

            // get the names of the data that contains file information
            Set<String> dbDataFileInfoNames = new HashSet<>();
            List<DataFileInfo> dataFileInfos = dataFileInfoRepository.findByDataFileAbsolutePath(baseDir);
            dataFileInfos.forEach(info -> {
                dbDataFileInfoNames.add(info.getDataFile().getName());
            });

            List<BasicFileInfo> result = FileInfos.listBasicPathInfo(files);
            result.forEach(info -> {
                DataListItem item = new DataListItem();
                item.setFileName(info.getFilename());
                item.setCreationDate(FilePrint.fileTimestamp(info.getCreationTime()));
                item.setFileName(info.getFilename());
                item.setSize(FilePrint.humanReadableSize(info.getSize(), true));

                if (dbFileName.contains(info.getFilename())) {
                    item.setAlert(!dbDataFileInfoNames.contains(info.getFilename()));
                } else {
                    DataFile file = new DataFile();
                    file.setAbsolutePath(info.getAbsolutePath().toString());
                    file.setCreationTime(new Date(info.getCreationTime()));
                    file.setFileSize(info.getSize());
                    file.setLastModifiedTime(new Date(info.getLastModifiedTime()));
                    file.setName(info.getFilename());
                    dataFileToSave.add(file);

                    item.setAlert(true);
                }

                listItems.add(item);
            });
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        if (!dataFileToSave.isEmpty()) {
            dataFileRepository.save(dataFileToSave);
        }

        return listItems;
    }

    public DataFileRepository getDataFileRepository() {
        return dataFileRepository;
    }

    public DataFileInfoRepository getDataFileInfoRepository() {
        return dataFileInfoRepository;
    }

}
