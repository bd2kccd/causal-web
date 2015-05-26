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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.pitt.dbmi.ccd.web.util.MessageDigestHash;

/**
 *
 * Apr 1, 2015 4:06:42 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class BigDataFileManager {

    private final String uploadDirectory;

    @Autowired(required = true)
    public BigDataFileManager(@Value("${app.uploadDir}") String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
        // create directory, if not exist, for uploaded data
        Path dataDir = Paths.get(uploadDirectory);
        if (Files.notExists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
            }
        }
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
     * @return
     * @throws IOException
     */
    public boolean chunkExists(String identifier, int chunkNumber, long chunkSize) throws IOException {
        Path chunkFile = Paths.get(uploadDirectory, identifier, chunkNumber + "");
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
     * @throws IOException
     */
    public void storeChunk(String identifier, int chunkNumber, InputStream inputStream) throws IOException {
        Path chunkFile = Paths.get(uploadDirectory, identifier, String.valueOf(chunkNumber));
        if (Files.notExists(chunkFile)) {
            try {
                Files.createDirectories(chunkFile);
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
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
     * @return true if all chunks of the file is on the server
     */
    public boolean allChunksUploaded(String identifier, long chunkSize, long totalSize, int numOfChunks) {
        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(uploadDirectory, identifier, String.valueOf(chunkNo)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merge all the chunks together into the original file. Delete the
     * directory containing the chunks. Compute MD5 hash on the original file.
     *
     * @param fileName the name given (usually original file name) to the merged
     * file
     * @param identifier unique name of the upload file
     * @param chunkSize size of each chunk
     * @param totalSize size of the original file
     * @param numOfChunks number of chunks file is broken into
     * @return MD5 hash of the original file
     * @throws IOException
     */
    public String mergeAndDeleteWithMd5(String fileName, String identifier, long chunkSize, final long totalSize, final int numOfChunks) throws IOException {
        Path newFilePath = Paths.get(uploadDirectory, fileName);
        Files.deleteIfExists(newFilePath); // delete the existing file
        try (BufferedOutputStream bos = new BufferedOutputStream(
        		new FileOutputStream(newFilePath.toFile(), false))) {
            for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
                Path chunkPath = Paths.get(uploadDirectory, identifier, String.valueOf(chunkNo));
                Files.copy(chunkPath, bos);
            }
        }

        String md5 = MessageDigestHash.computeMD5Hash(newFilePath);
        try {
            deleteNonEmptyDir(Paths.get(uploadDirectory, identifier));
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
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

    /**
     * Get the directory where all files are uploaded.
     *
     * @return path to uploaded files
     */
    public String getUploadDirectory() {
        return uploadDirectory;
    }

}
