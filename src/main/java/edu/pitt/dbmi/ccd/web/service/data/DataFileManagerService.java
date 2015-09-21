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
package edu.pitt.dbmi.ccd.web.service.data;

import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.data.ResumableChunk;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 20, 2015 7:58:08 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class DataFileManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileManagerService.class);

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    @Autowired(required = true)
    public DataFileManagerService(UserAccountService userAccountService, DataFileService dataFileService) {
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
    }

    public boolean isSupported(ResumableChunk chunk) {
        return true;
    }

    public boolean chunkExists(ResumableChunk chunk, AppUser appUser) throws IOException {
        String directory = appUser.getDataDirectory();
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());
        long chunkSize = chunk.getResumableChunkSize();

        Path chunkFile = Paths.get(directory, identifier, chunkNumber);
        if (Files.exists(chunkFile)) {
            long size = (Long) Files.getAttribute(chunkFile, "basic:size");
            return size == chunkSize;
        }

        return false;
    }

    public void storeChunk(ResumableChunk chunk, AppUser appUser) throws IOException {
        String directory = appUser.getDataDirectory();
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());
        InputStream inputStream = chunk.getFile().getInputStream();

        Path chunkFile = Paths.get(directory, identifier, chunkNumber);
        if (Files.notExists(chunkFile)) {
            try {
                Files.createDirectories(chunkFile);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
        Files.copy(inputStream, chunkFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public boolean allChunksUploaded(ResumableChunk chunk, AppUser appUser) {
        String directory = appUser.getDataDirectory();
        String identifier = chunk.getResumableIdentifier();
        int numOfChunks = chunk.getResumableTotalChunks();

        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(directory, identifier, Integer.toString(chunkNo)))) {
                return false;
            }
        }

        return true;
    }

    public String mergeDeleteSave(ResumableChunk chunk, AppUser appUser) throws IOException {
        String directory = appUser.getDataDirectory();
        String fileName = chunk.getResumableFilename();
        int numOfChunks = chunk.getResumableTotalChunks();
        String identifier = chunk.getResumableIdentifier();

        Path newFile = Paths.get(directory, fileName);
        Files.deleteIfExists(newFile); // delete the existing file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile.toFile(), false))) {
            for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
                Path chunkPath = Paths.get(directory, identifier, Integer.toString(chunkNo));
                Files.copy(chunkPath, bos);
            }
        }

        String md5checkSum = saveDataFile(newFile, appUser.getUsername());
        try {
            deleteNonEmptyDir(Paths.get(directory, identifier));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return md5checkSum;
    }

    private String saveDataFile(Path file, String username) throws IOException {
        UserAccount userAccount = userAccountService.findByUsername(username);

        BasicFileInfo fileInfo = FileInfos.basicPathInfo(file);
        String directory = fileInfo.getAbsolutePath().toString();
        String fileName = fileInfo.getFilename();
        long size = fileInfo.getSize();
        long creationTime = fileInfo.getCreationTime();
        long lastModifiedTime = fileInfo.getLastModifiedTime();

        DataFile dataFile = dataFileService.findByAbsolutePathAndName(directory, fileName);
        if (dataFile == null) {
            dataFile = new DataFile();
            dataFile.setUserAccounts(Collections.singleton(userAccount));
        }
        dataFile.setName(fileName);
        dataFile.setAbsolutePath(directory);
        dataFile.setCreationTime(new Date(creationTime));
        dataFile.setFileSize(size);
        dataFile.setLastModifiedTime(new Date(lastModifiedTime));

        String md5checkSum = edu.pitt.dbmi.ccd.commons.file.MessageDigestHash.computeMD5Hash(file);
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        if (dataFileInfo == null) {
            dataFileInfo = new DataFileInfo();
        }
        dataFileInfo.setFileDelimiter(null);
        dataFileInfo.setMd5checkSum(md5checkSum);
        dataFileInfo.setMissingValue(null);
        dataFileInfo.setNumOfColumns(null);
        dataFileInfo.setNumOfRows(null);
        dataFileInfo.setVariableType(null);

        dataFile.setDataFileInfo(dataFileInfo);
        dataFileService.saveDataFile(dataFile);

        return md5checkSum;
    }

    /**
     * Delete folder/directory. If the directory is non-empty, all the files and
     * folder in the directory will be deleted.
     *
     * @param path path to folder to be deleted
     * @throws IOException
     */
    private void deleteNonEmptyDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
                if (exception == null) {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exception;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
