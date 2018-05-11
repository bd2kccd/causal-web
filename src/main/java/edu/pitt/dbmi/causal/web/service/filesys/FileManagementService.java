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
package edu.pitt.dbmi.causal.web.service.filesys;

import edu.pitt.dbmi.causal.web.prop.CcdProperties;
import edu.pitt.dbmi.causal.web.util.FileSys;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public static final String DATA_FOLDER = "data";
    public static final String RESULT_FOLDER = "results";

    private final CcdProperties ccdProperties;
    private final FileService fileService;

    @Autowired
    public FileManagementService(CcdProperties ccdProperties, FileService fileService) {
        this.ccdProperties = ccdProperties;
        this.fileService = fileService;
    }

    public Path getLocalFile(File file, UserAccount userAccount) {
        String rootDir = ccdProperties.getWorkspaceDir();
        String userFolder = userAccount.getAccount();
        String fileName = file.getName();

        return Paths.get(rootDir, userFolder, DATA_FOLDER, fileName);
    }

    public boolean deleteFile(File file, UserAccount userAccount) {
        try {
            Path physicalFile = getLocalFile(file, userAccount);
            Files.deleteIfExists(physicalFile);

            fileService.getRepository().delete(file);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());

            return false;
        }

        return true;
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
                dbFileMap.forEach((k, v) -> files.add(v));
                fileService.getRepository().deleteInBatch(files);
            }

            if (!filesToSave.isEmpty()) {
                fileService.persistLocalFiles(filesToSave, DATA_FOLDER, userAccount);
            }
        }
    }

    public void setupUserHomeDirectory(UserAccount userAccount) {
        List<Path> directories = new LinkedList<>();
        directories.add(getUserDataDirectory(userAccount));
        directories.add(getUserResultDirectory(userAccount));

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

    public List<String> listResultFiles(String folder, UserAccount userAccount) {
        List<String> files = new LinkedList<>();

        try {
            String root = getUserResultDirectory(userAccount).toString();
            Files.walk(Paths.get(root, folder))
                    .filter(Files::isRegularFile)
                    .map(e -> e.getFileName().toString())
                    .collect(Collectors.toCollection(() -> files));
        } catch (IOException exception) {
            LOGGER.error("", exception);
        }

        return files;
    }

}
