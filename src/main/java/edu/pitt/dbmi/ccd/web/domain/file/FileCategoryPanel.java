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

/**
 *
 * Jul 6, 2017 2:18:43 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileCategoryPanel {

    private String title;
    private String description;
    private String panelColor;
    private List<FileCategory> fileCategories;

    public FileCategoryPanel() {
    }

    public FileCategoryPanel(String title, String description, String panelColor, List<FileCategory> fileCategories) {
        this.title = title;
        this.description = description;
        this.panelColor = panelColor;
        this.fileCategories = fileCategories;
    }

    public List<FileCategory> getFileCategories() {
        return fileCategories;
    }

    public void setFileCategories(List<FileCategory> fileCategories) {
        this.fileCategories = fileCategories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPanelColor() {
        return panelColor;
    }

    public void setPanelColor(String panelColor) {
        this.panelColor = panelColor;
    }

}
