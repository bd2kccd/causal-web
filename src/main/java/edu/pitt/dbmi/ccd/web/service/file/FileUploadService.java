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

import edu.pitt.dbmi.ccd.commons.file.FileMD5Hash;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfos;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.web.domain.file.ResumableChunk;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Apr 26, 2017 1:58:20 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

    private final FileManagementService fileManagementService;
    private final FileService fileService;

    @Autowired
    public FileUploadService(FileManagementService fileManagementService, FileService fileService) {
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
    }

    public File saveFileToDatabase(Path file, UserAccount userAccount) {
        File savedFileEntity = null;

        try {
            BasicFileInfo fileInfo = BasicFileInfos.getBasicFileInfo(file);
            String name = fileInfo.getFilename();
            String title = fileInfo.getFilename();
            Date creationTime = new Date(fileInfo.getCreationTime());
            long fileSize = fileInfo.getSize();
            String md5checkSum = FileMD5Hash.computeHash(file);

            File fileEntity = new File();
            fileEntity.setCreationTime(creationTime);
            fileEntity.setFileSize(fileSize);
            fileEntity.setMd5checkSum(md5checkSum);
            fileEntity.setName(name);
            fileEntity.setTitle(title);
            fileEntity.setUserAccount(userAccount);

            savedFileEntity = fileService.getFileRepository().save(fileEntity);
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return savedFileEntity;
    }

    public Path combineAllChunks(ResumableChunk chunk, UserAccount userAccount) {
        String fileName = chunk.getResumableFilename();
        String identifier = chunk.getResumableIdentifier();
        int numOfChunks = chunk.getResumableTotalChunks();
        String usrDataDir = fileManagementService.getUserDataDirectory(userAccount);

        Path combinedFile = Paths.get(usrDataDir, fileName);

        // delete the existing file
        try {
            Files.deleteIfExists(combinedFile);
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
            return null;
        }

        // combine the file chunks
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(combinedFile.toFile(), false))) {
            for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
                Path chunkPath = Paths.get(usrDataDir, identifier, Integer.toString(chunkNo));
                Files.copy(chunkPath, bos);
            }
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
            return null;
        }

        // delete the chunk's folder
        try {
            fileManagementService.deleteNonEmptyDir(Paths.get(usrDataDir, identifier));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return combinedFile;
    }

    public boolean allChunksUploaded(ResumableChunk chunk, UserAccount userAccount) {
        String usrDataDir = fileManagementService.getUserDataDirectory(userAccount);
        String identifier = chunk.getResumableIdentifier();
        int numOfChunks = chunk.getResumableTotalChunks();
        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(usrDataDir, identifier, Integer.toString(chunkNo)))) {
                return false;
            }
        }

        return true;
    }

    public boolean saveChunk(ResumableChunk chunk, UserAccount userAccount) {
        try {
            Path chunkFile = createPathToChunkFile(chunk, userAccount);
            if (Files.notExists(chunkFile)) {
                Files.createDirectories(chunkFile);
            }
            Files.copy(chunk.getFile().getInputStream(), chunkFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
            return false;
        }

        return true;
    }

    public boolean isSupported(ResumableChunk chunk) {
        return true;
    }

    public boolean chunkExists(ResumableChunk chunk, UserAccount userAccount) {
        boolean fileExist = false;

        Path chunkFile = createPathToChunkFile(chunk, userAccount);
        if (Files.exists(chunkFile)) {
            try {
                long chunkSize = (Long) Files.getAttribute(chunkFile, "basic:size");
                fileExist = (chunkSize == chunk.getResumableChunkSize());
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        return fileExist;
    }

    private Path createPathToChunkFile(ResumableChunk chunk, UserAccount userAccount) {
        String usrDataDir = fileManagementService.getUserDataDirectory(userAccount);
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());

        return Paths.get(usrDataDir, identifier, chunkNumber);
    }

}
