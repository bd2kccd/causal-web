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
package edu.pitt.dbmi.ccd.web.util;

import edu.pitt.dbmi.ccd.web.model.FileInfo;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Apr 9, 2015 11:21:55 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileUtility {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private FileUtility() {
    }

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String humanReadableSize(long fileSize, boolean si) {
        int unit = si ? 1000 : 1024;
        if (fileSize < unit) {
            return fileSize + " B";
        }
        int exp = (int) (Math.log(fileSize) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

        return String.format("%.2f %sB", fileSize / Math.pow(unit, exp), pre);
    }

    public static List<FileInfo> getFileListing(Path directory) {
        List<FileInfo> list = new LinkedList<>();

        List<FileInfoMeta> fileMeta = new LinkedList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path path : directoryStream) {
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                FileInfoMeta info = new FileInfoMeta();
                info.setFileName(path.getFileName().toString());
                info.setSize(attrs.size());
                info.setCreationDate(new Date(attrs.creationTime().toMillis()));
                fileMeta.add(info);
            }
        } catch (IOException exception) {
        }

        Collections.sort(fileMeta, new FileDateSort());
        for (FileInfoMeta info : fileMeta) {
            FileInfo resultFile = new FileInfo();
            resultFile.setCreationDate(FileUtility.formatDate(info.getCreationDate()));
            resultFile.setFileName(info.getFileName());
            resultFile.setSize(FileUtility.humanReadableSize(info.getSize(), true));
            list.add(resultFile);
        }

        return list;
    }

    private static class FileDateSort implements Comparator<FileInfoMeta> {

        @Override
        public int compare(FileInfoMeta o1, FileInfoMeta o2) {
            return o1.creationDate.compareTo(o2.creationDate);
        }

    }

    private static class FileInfoMeta {

        private String fileName;

        private long size;

        private Date creationDate;

        public FileInfoMeta() {
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
        }
    }

}
