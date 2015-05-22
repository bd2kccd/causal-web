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
 * May 22, 2015 2:26:39 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 * 
 */
public class FileMetadata {

    private String fileName;

    private String size;

    private String lastModifiedDate;

    public FileMetadata(){
    	
    }
    
	/**
	 * @param fileName
	 * @param size
	 * @param creationDate
	 */
	public FileMetadata(String fileName, String size, String lastModifiedDate) {
		super();
		this.fileName = fileName;
		this.size = size;
		this.lastModifiedDate = lastModifiedDate;
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

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

}
