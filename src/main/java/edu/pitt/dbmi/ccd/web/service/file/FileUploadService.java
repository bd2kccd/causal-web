/*
 * Copyright (C) 2016 University of Pittsburgh.
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
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.ResumableChunk;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.util.FileSystem;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 2, 2016 10:54:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

    private final UserAccountService userAccountService;

    private final FileService fileService;

    private final FileManagementService fileManagementService;

    private final CcdProperties ccdProperties;

    @Autowired
    public FileUploadService(UserAccountService userAccountService, FileService fileService, FileManagementService fileManagementService, CcdProperties ccdProperties) {
        this.userAccountService = userAccountService;
        this.fileService = fileService;
        this.fileManagementService = fileManagementService;
        this.ccdProperties = ccdProperties;
    }

    public void checkChunkExistence(ResumableChunk chunk, AppUser appUser, HttpServletResponse response) {
        if (chunkExists(chunk, appUser)) {
            response.setStatus(200); // do not upload chunk again
        } else {
            response.setStatus(404); // chunk not on the server, upload it
        }
    }

    public void processChunkUpload(ResumableChunk chunk, AppUser appUser, HttpServletResponse response) throws IOException {
        if (!isSupported(chunk)) {
            response.getWriter().println("Not Supported");
            response.setStatus(501); // cancel the whole upload
            return;
        }

        try {
            UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
            String workspace = userAccount.getPerson().getWorkspace();

            saveChunk(chunk, workspace);
            if (allChunksUploaded(chunk, workspace)) {
                Path uploadedFile = combineChunks(chunk, workspace);
                File newFileEntity = fileManagementService.createFileEntity(uploadedFile, userAccount);
                synchronized (fileService) {
                    String absolutePath = newFileEntity.getAbsolutePath();
                    String name = newFileEntity.getName();
                    File fileEntity = fileService.findByAbsolutePathAndName(absolutePath, name);
                    if (fileEntity == null) {
                        fileEntity = newFileEntity;
                    } else {
                        fileEntity.setCreationTime(newFileEntity.getCreationTime());
                        fileEntity.setFileSize(newFileEntity.getFileSize());
                        fileEntity.setMd5checkSum(newFileEntity.getMd5checkSum());
                    }
                    fileService.save(fileEntity);
                }

                response.getWriter().println(newFileEntity.getMd5checkSum());
            }
            response.setStatus(200);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            response.getWriter().println("Failed");
            response.setStatus(501); // cancel the whole upload
        }
    }

    private Path combineChunks(ResumableChunk chunk, String workspace) throws IOException {
        String dataFolder = ccdProperties.getDataFolder();
        String fileName = chunk.getResumableFilename();
        int numOfChunks = chunk.getResumableTotalChunks();
        String identifier = chunk.getResumableIdentifier();

        Path combinedFile = Paths.get(workspace, dataFolder, fileName);
        Files.deleteIfExists(combinedFile); // delete the existing file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(combinedFile.toFile(), false))) {
            for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
                Path chunkPath = Paths.get(workspace, dataFolder, identifier, Integer.toString(chunkNo));
                Files.copy(chunkPath, bos);
            }
        }

        // clean up
        Path tmpDir = Paths.get(workspace, dataFolder, identifier);
        try {
            FileSystem.deleteNonEmptyDir(tmpDir);
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return combinedFile;
    }

    private boolean allChunksUploaded(ResumableChunk chunk, String workspace) {
        String dataFolder = ccdProperties.getDataFolder();
        String identifier = chunk.getResumableIdentifier();
        int numOfChunks = chunk.getResumableTotalChunks();

        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(workspace, dataFolder, identifier, Integer.toString(chunkNo)))) {
                return false;
            }
        }

        return true;
    }

    private void saveChunk(ResumableChunk chunk, String workspace) throws IOException {
        String dataFolder = ccdProperties.getDataFolder();
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());

        Path chunkFile = Paths.get(workspace, dataFolder, identifier, chunkNumber);
        if (Files.notExists(chunkFile)) {
            Files.createDirectories(chunkFile);
        }
        Files.copy(chunk.getFile().getInputStream(), chunkFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isSupported(ResumableChunk chunk) {
        return true;
    }

    private boolean chunkExists(ResumableChunk chunk, AppUser appUser) {
        UserAccount userAccount = null;
        try {
            userAccount = userAccountService.findByUsername(appUser.getUsername());
        } catch (Exception exception) {
            LOGGER.error(exception.getLocalizedMessage());
        }
        if (userAccount == null) {
            return false;
        }

        String workspace = userAccount.getPerson().getWorkspace();
        String dataFolder = ccdProperties.getDataFolder();
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());
        long chunkSize = chunk.getResumableChunkSize();

        boolean fileExist = false;
        Path chunkFile = Paths.get(workspace, dataFolder, identifier, chunkNumber);
        if (Files.exists(chunkFile)) {
            try {
                long size = (Long) Files.getAttribute(chunkFile, "basic:size");
                fileExist = (size == chunkSize);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        return fileExist;
    }

}
