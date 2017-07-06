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

/**
 *
 * Jul 6, 2017 4:26:17 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileListView {

    private String pageTitle;
    private String title;
    private String fileFormatName;

    public FileListView() {
    }

    public FileListView(String pageTitle, String title, String fileFormatName) {
        this.pageTitle = pageTitle;
        this.title = title;
        this.fileFormatName = fileFormatName;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileFormatName() {
        return fileFormatName;
    }

    public void setFileFormatName(String fileFormatName) {
        this.fileFormatName = fileFormatName;
    }

}
