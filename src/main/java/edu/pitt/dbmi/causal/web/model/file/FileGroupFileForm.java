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
package edu.pitt.dbmi.causal.web.model.file;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * Sep 10, 2018 11:24:18 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileGroupFileForm {

    @NotNull
    private Long varTypeId;

    @NotEmpty(message = "Please select at least one file.")
    private List<Long> fileIds;

    public FileGroupFileForm() {
    }

    public FileGroupFileForm(Long varTypeId, List<Long> fileIds) {
        this.varTypeId = varTypeId;
        this.fileIds = fileIds;
    }

    @Override
    public String toString() {
        return "FileGroupFileForm{" + "varTypeId=" + varTypeId + ", fileIds=" + fileIds + '}';
    }

    public Long getVarTypeId() {
        return varTypeId;
    }

    public void setVarTypeId(Long varTypeId) {
        this.varTypeId = varTypeId;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

}
