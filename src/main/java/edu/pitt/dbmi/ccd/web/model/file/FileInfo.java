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
package edu.pitt.dbmi.ccd.web.model.file;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import java.util.Date;

/**
 *
 * Nov 13, 2015 8:56:03 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileInfo {

    protected String fileName;

    protected Long fileSize;

    protected Date creationDate;

    public FileInfo() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileSizePrint() {
        return FilePrint.humanReadableSize(fileSize, true);
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getCreationDatePrint() {
        return FilePrint.fileTimestamp(creationDate.getTime());
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
