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
package edu.pitt.dbmi.ccd.web.domain;

import java.util.Date;
import org.springframework.context.annotation.Scope;

/**
 *
 * May 18, 2015 9:32:20 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Scope("session")
public class AppUser {

    private String username;

    private String firstName;

    private String lastName;

    private String uploadDirectory;

    private String outputDirectory;

    private String libDirectory;

    private String tmpDirectory;

    private Date lastLoginDate;

    private boolean webUser;

    public AppUser() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getLibDirectory() {
        return libDirectory;
    }

    public void setLibDirectory(String libDirectory) {
        this.libDirectory = libDirectory;
    }

    public String getTmpDirectory() {
        return tmpDirectory;
    }

    public void setTmpDirectory(String tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public boolean isWebUser() {
        return webUser;
    }

    public void setWebUser(boolean webUser) {
        this.webUser = webUser;
    }

}
