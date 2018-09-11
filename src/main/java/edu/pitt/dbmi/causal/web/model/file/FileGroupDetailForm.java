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

import javax.validation.constraints.NotBlank;

/**
 *
 * Sep 10, 2018 11:22:26 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileGroupDetailForm {

    @NotBlank(message = "Please enter a name for the file group.")
    private String name;

    private String description;

    public FileGroupDetailForm() {
    }

    public FileGroupDetailForm(String name) {
        this.name = name;
    }

    public FileGroupDetailForm(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
