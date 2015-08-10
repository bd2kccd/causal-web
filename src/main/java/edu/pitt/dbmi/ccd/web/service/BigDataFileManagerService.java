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

import edu.pitt.dbmi.ccd.web.util.MessageDigestHash;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * Apr 1, 2015 4:06:42 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class BigDataFileManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigDataFileManagerService.class);

    public BigDataFileManagerService() {
    }

    /**
     * Determine if we should allow this type of file to be uploaded on the
     * server.
     *
     * @param fileName
     * @return true if the file is allowed to be uploaded
     */
    public boolean isSupported(String fileName) {
        return true;
    }

    /**
     * Check to see if the chuck is already uploaded.
     *
     * @param identifier
     * @param chunkNumber
     * @param chunkSize
     * @param directory
     * @return
     * @throws IOException
     */
    public boolean chunkExists(String identifier, int chunkNumber, long chunkSize, String directory) throws IOException {
        Path chunkFile = Paths.get(directory, identifier, chunkNumber + "");
        if (Files.exists(chunkFile)) {
            long size = (Long) Files.getAttribute(chunkFile, "basic:size");
            return size == chunkSize;
        }

        return false;
    }

    /**
     * Save the file chunk on the server.
     *
     * @param identifier
     * @param chunkNumber
     * @param inputStream
     * @param directory
     * @throws IOException
     */
    public void storeChunk(String identifier, int chunkNumber, InputStream inputStream, String directory) throws IOException {
        Path chunkFile = Paths.get(directory, identifier, String.valueOf(chunkNumber));
        if (Files.notExists(chunkFile)) {
            try {
                Files.createDirectories(chunkFile);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
        Files.copy(inputStream, chunkFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Check to see if the file upload is completed.
     *
     * @param identifier
     * @param chunkSize
     * @param totalSize
     * @param numOfChunks
     * @param directory
     * @return true if all chunks of the file is on the server
     */
    public boolean allChunksUploaded(String identifier, long chunkSize, long totalSize, int numOfChunks, String directory) {
        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(directory, identifier, String.valueOf(chunkNo)))) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param fileName the name given (usually original file name) to the merged
     * file
     * @param identifier unique name of the upload file
     * @param chunkSize size of each chunk
     * @param totalSize size of the original file
     * @param numOfChunks number of chunks file is broken into
     * @param directory
     * @return MD5 hash of the original file
     * @throws IOException
     */
    public String mergeAndDeleteWithMd5(String fileName, String identifier, long chunkSize, final long totalSize, final int numOfChunks, final String directory) throws IOException {
        Path newFilePath = Paths.get(directory, fileName);
        Files.deleteIfExists(newFilePath); // delete the existing file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFilePath.toFile(), false))) {
            for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
                Path chunkPath = Paths.get(directory, identifier, String.valueOf(chunkNo));
                Files.copy(chunkPath, bos);
            }
        }

        String md5 = MessageDigestHash.computeMD5Hash(newFilePath);
        try {
            deleteNonEmptyDir(Paths.get(directory, identifier));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }
        return md5;
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
