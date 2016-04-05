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
package edu.pitt.dbmi.ccd.web.model.user;

import edu.pitt.dbmi.ccd.db.entity.SecurityQuestion;

/**
 *
 * Jun 20, 2015 12:20:50 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UserRegistration {

    private String username;

    private String email;

    private String password;

    private SecurityQuestion secureQues;

    private String secureAns;

    private boolean agree;

    public UserRegistration() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SecurityQuestion getSecureQues() {
        return secureQues;
    }

    public void setSecureQues(SecurityQuestion secureQues) {
        this.secureQues = secureQues;
    }

    public String getSecureAns() {
        return secureAns;
    }

    public void setSecureAns(String secureAns) {
        this.secureAns = secureAns;
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }

}