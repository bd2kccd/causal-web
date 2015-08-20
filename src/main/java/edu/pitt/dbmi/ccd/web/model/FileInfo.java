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
package edu.pitt.dbmi.ccd.web.model;

/**
 *
 * Apr 9, 2015 12:38:48 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileInfo {

    private String fileName;

    // Added to support directory browsing
    private String filePath;

    private String size;

    private String creationDate;

    private long rawCreationDate;

    private boolean onCloud;

    public FileInfo(String fileName, String filePath, String size, String creationDate, long rawCreationDate, boolean onCloud) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.size = size;
        this.creationDate = creationDate;
        this.rawCreationDate = rawCreationDate;
        this.onCloud = onCloud;
    }

    public FileInfo() {
        this(null, null, null);
    }

    public FileInfo(String fileName, String size, String creationDate) {
        this(fileName, null, size, creationDate, 0, false);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public long getRawCreationDate() {
        return rawCreationDate;
    }

    public void setRawCreationDate(long rawCreationDate) {
        this.rawCreationDate = rawCreationDate;
    }

    public boolean isOnCloud() {
        return onCloud;
    }

    public void setOnCloud(boolean onCloud) {
        this.onCloud = onCloud;
    }

}
