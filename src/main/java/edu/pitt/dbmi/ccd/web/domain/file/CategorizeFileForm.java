/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.domain.file;

import javax.validation.constraints.NotNull;

/**
 *
 * May 14, 2017 12:05:20 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class CategorizeFileForm {

    @NotNull
    private Long fileTypeId;

    @NotNull
    private Long fileFormatId;

    public CategorizeFileForm() {
    }

    @Override
    public String toString() {
        return "CategorizeFileForm{" + "fileTypeId=" + fileTypeId + ", fileFormatId=" + fileFormatId + '}';
    }

    public Long getFileTypeId() {
        return fileTypeId;
    }

    public void setFileTypeId(Long fileTypeId) {
        this.fileTypeId = fileTypeId;
    }

    public Long getFileFormatId() {
        return fileFormatId;
    }

    public void setFileFormatId(Long fileFormatId) {
        this.fileFormatId = fileFormatId;
    }

}
