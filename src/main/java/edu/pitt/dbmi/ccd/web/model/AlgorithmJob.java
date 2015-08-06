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
 * Aug 3, 2015 10:22:18 AM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
public class AlgorithmJob {

	private Long id;
	private String algorName;
	private String fileName;
	private String status;
	private String addedTime;

	/**
	 * @param id
	 * @param algorName
	 * @param fileName
	 * @param status
	 * @param addedTime
	 */
	public AlgorithmJob(Long id, String algorName, String fileName, String status, String addedTime) {
		this.id = id;
		this.algorName = algorName;
		this.fileName = fileName;
		this.status = status;
		this.addedTime = addedTime;
	}

	public Long getId() {
		return id;
	}

	public void setPid(Long id) {
		this.id = id;
	}

	public String getAlgorName() {
		return algorName;
	}

	public void setAlgorName(String algorName) {
		this.algorName = algorName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAddedTime() {
		return addedTime;
	}

	public void setAddedTime(String addedTime) {
		this.addedTime = addedTime;
	}

}
