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

import java.util.List;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * Jun 29, 2017 11:02:23 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileGroupForm {

    @NotBlank(message = "Please enter a name for the file group.")
    private String groupName;

    @NotNull
    private Long fileVariableTypeId;

    @NotEmpty(message = "Please select at least one file.")
    private List<Long> fileIds;

    public FileGroupForm() {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getFileVariableTypeId() {
        return fileVariableTypeId;
    }

    public void setFileVariableTypeId(Long fileVariableTypeId) {
        this.fileVariableTypeId = fileVariableTypeId;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

}
