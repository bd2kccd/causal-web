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
package edu.pitt.dbmi.causal.web.service.file;

import edu.pitt.dbmi.causal.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.commons.file.FileSys;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiterType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterTypeService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.data.Delimiter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 5, 2016 12:11:29 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagementService.class);

    private final String DATA_FOLDER = "data";
    private final String RESULT_FOLDER = "results";

    private final String[] RESULT_SUBFOLDERS = {
        "tetrad"
    };

    private final CcdProperties ccdProperties;
    private final FileService fileService;

    @Autowired
    public FileManagementService(CcdProperties ccdProperties, FileService fileService) {
        this.ccdProperties = ccdProperties;
        this.fileService = fileService;
    }

    public void deleteFile(File file, UserAccount userAccount) {
        try {
            Path physicalFile = getPhysicalFile(file, userAccount);
            Files.deleteIfExists(physicalFile);
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        fileService.getRepository().delete(file);
    }

    public Delimiter getReaderFileDelimiter(FileDelimiterType fileDelimiterType) {
        String fileDelimName = (fileDelimiterType == null) ? "" : fileDelimiterType.getName();
        switch (fileDelimName) {
            case FileDelimiterTypeService.COLON_DELIM_NAME:
                return Delimiter.COLON;
            case FileDelimiterTypeService.COMMA_DELIM_NAME:
                return Delimiter.COMMA;
            case FileDelimiterTypeService.PIPE_DELIM_NAME:
                return Delimiter.PIPE;
            case FileDelimiterTypeService.SEMICOLON_DELIM_NAME:
                return Delimiter.SEMICOLON;
            case FileDelimiterTypeService.SPACE_DELIM_NAME:
                return Delimiter.SPACE;
            case FileDelimiterTypeService.TAB_DELIM_NAME:
                return Delimiter.TAB;
            case FileDelimiterTypeService.WHITESPACE_DELIM_NAME:
                return Delimiter.WHITESPACE;
            default:
                return Delimiter.TAB;
        }
    }

    public void syncFilesWithDatabase(UserAccount userAccount) {
        List<Path> localFiles = new LinkedList<>();
        try {
            Path userDataDir = getUserDataDirectory(userAccount);
            localFiles.addAll(FileSys.listFilesInDirectory(userDataDir, false));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        if (localFiles.isEmpty()) {
            fileService.getRepository().deleteAll();
        } else {
            // grab all the files from the database
            Map<String, File> dbFileMap = new HashMap<>();
            List<File> dbFileList = fileService.getRepository().findByUserAccount(userAccount);
            dbFileList.forEach(file -> {
                dbFileMap.put(file.getName(), file);
            });

            List<Path> filesToSave = new LinkedList<>();
            localFiles.forEach(localFile -> {
                String fileName = localFile.getFileName().toString();
                if (dbFileMap.containsKey(fileName)) {
                    dbFileMap.remove(fileName);
                } else {
                    filesToSave.add(localFile);
                }
            });

            if (!dbFileMap.isEmpty()) {
                List<File> files = new LinkedList<>();
                dbFileMap.forEach((key, value) -> {
                    files.add(value);
                });
                fileService.getRepository().delete(files);
            }

            if (!filesToSave.isEmpty()) {
                fileService.persistLocalFiles(filesToSave, userAccount);
            }
        }
    }

    public Path getPhysicalFile(File file, UserAccount userAccount) {
        String rootDir = ccdProperties.getWorkspaceDir();
        String userFolder = userAccount.getAccount();
        String fileName = file.getName();

        return Paths.get(rootDir, userFolder, DATA_FOLDER, fileName);
    }

    public Path getUserHomeDirectory(UserAccount userAccount) {
        String rootDir = ccdProperties.getWorkspaceDir();
        String userFolder = userAccount.getAccount();

        return Paths.get(rootDir, userFolder);
    }

    public Path getUserDataDirectory(UserAccount userAccount) {
        String rootDir = ccdProperties.getWorkspaceDir();
        String userFolder = userAccount.getAccount();

        return Paths.get(rootDir, userFolder, DATA_FOLDER);
    }

    public Path getUserResultDirectory(UserAccount userAccount) {
        String rootDir = ccdProperties.getWorkspaceDir();
        String userFolder = userAccount.getAccount();

        return Paths.get(rootDir, userFolder, RESULT_FOLDER);
    }

    public void setupUserHomeDirectory(UserAccount userAccount) {
        String workspace = ccdProperties.getWorkspaceDir();
        String homeFolder = userAccount.getAccount();

        List<Path> directories = new LinkedList<>();
        directories.add(Paths.get(workspace, homeFolder, DATA_FOLDER));
        directories.add(Paths.get(workspace, homeFolder, RESULT_FOLDER));

        // subdirectories
        for (String subFolder : RESULT_SUBFOLDERS) {
            directories.add(Paths.get(workspace, homeFolder, RESULT_FOLDER, subFolder));
        }

        directories.forEach(directory -> {
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to create directory '%s'.", directory), exception);
                }
            }
        });
    }

}
