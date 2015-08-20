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
package edu.pitt.dbmi.ccd.web.service.cloud.dto;

import java.util.Objects;

/**
 *
 * Aug 19, 2015 2:21:41 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileInfoResponse {

    private String fileName;

    private long size;

    private long creationDate;

    public FileInfoResponse() {
    }

    public FileInfoResponse(String fileName, long size, long creationDate) {
        this.fileName = fileName;
        this.size = size;
        this.creationDate = creationDate;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.fileName);
        hash = 53 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 53 * hash + (int) (this.creationDate ^ (this.creationDate >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileInfoResponse other = (FileInfoResponse) obj;
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        if (this.creationDate != other.creationDate) {
            return false;
        }
        return true;
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

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

}
