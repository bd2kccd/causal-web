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
package edu.pitt.dbmi.ccd.web.model.data;

/**
 *
 * Jul 21, 2015 9:11:57 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class DataListItem {

    private String fileName;

    private String creationDate;

    private String size;

    private boolean onCloud;

    private String delimiter;

    private String variableType;

    public DataListItem() {
    }

    public DataListItem(String fileName, String creationDate, String size) {
        this(fileName, creationDate, size, false, null, null);
    }

    public DataListItem(String fileName, String creationDate, String size, boolean onCloud, String delimiter, String variableType) {
        this.fileName = fileName;
        this.creationDate = creationDate;
        this.size = size;
        this.onCloud = onCloud;
        this.delimiter = delimiter;
        this.variableType = variableType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isOnCloud() {
        return onCloud;
    }

    public void setOnCloud(boolean onCloud) {
        this.onCloud = onCloud;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

}
