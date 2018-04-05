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
package edu.pitt.dbmi.causal.web.model;

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

    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;

    private String username;

    private Date lastLogin;

    private boolean federatedUser;

    public AppUser() {
    }

    public void updateFullName() {
        fullName = String.format("%s %s %s",
                (firstName == null) ? "" : firstName,
                (middleName == null) ? "" : middleName,
                (lastName == null) ? "" : lastName);
        fullName = fullName.replaceAll("\\s+", " ").trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        if (fullName == null) {
            updateFullName();
        }

        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isFederatedUser() {
        return federatedUser;
    }

    public void setFederatedUser(boolean federatedUser) {
        this.federatedUser = federatedUser;
    }

}
