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
 * Aug 21, 2015 9:08:38 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class ResultFileInfo implements Comparable<ResultFileInfo> {

    private String fileName;

    private String size;

    private String creationDate;

    private long rawCreationDate;

    private boolean error;

    private boolean onCloud;

    public ResultFileInfo(String fileName, String size, String creationDate, long rawCreationDate, boolean error, boolean onCloud) {
        this.fileName = fileName;
        this.size = size;
        this.creationDate = creationDate;
        this.rawCreationDate = rawCreationDate;
        this.error = error;
        this.onCloud = onCloud;
    }

    public ResultFileInfo(String fileName, String size, String creationDate, long rawCreationDate, boolean error) {
        this.fileName = fileName;
        this.size = size;
        this.creationDate = creationDate;
        this.rawCreationDate = rawCreationDate;
        this.error = error;
    }

    public ResultFileInfo() {
    }

    @Override
    public int compareTo(ResultFileInfo o) {
        return Long.signum(this.rawCreationDate - o.rawCreationDate);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isOnCloud() {
        return onCloud;
    }

    public void setOnCloud(boolean onCloud) {
        this.onCloud = onCloud;
    }

}
