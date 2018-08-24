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
package edu.pitt.dbmi.causal.web.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * May 1, 2017 2:18:43 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileSys {

    /**
     * Retrieve all the files in the directory.
     *
     * @param dir directory to search for files
     * @param showHidden retrieve hidden files
     * @return
     * @throws IOException when unable to read the directory
     */
    public static List<Path> getFilesInDirectory(Path dir, boolean showHidden) throws IOException {
        List<Path> list = new LinkedList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir, path -> {
            return showHidden
                    ? Files.isRegularFile(path)
                    : Files.isRegularFile(path) && !Files.isHidden(path);
        })) {
            directoryStream.forEach(list::add);
        }

        return list;
    }

    public static List<Path> listAllInDirectory(Path dir, boolean showHidden) throws IOException {
        List<Path> list = new LinkedList<>();

        if (showHidden) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
                directoryStream.forEach(list::add);
            }
        } else {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir, path -> {
                return !Files.isHidden(path);
            })) {
                directoryStream.forEach(list::add);
            }
        }

        return list;
    }

    /**
     * Delete folder/directory. If the directory is non-empty, all the files and
     * folder in the directory will be deleted.
     *
     * @param path path to folder to be deleted
     * @throws IOException
     */
    public static void deleteNonEmptyDir(Path path) throws IOException {
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
