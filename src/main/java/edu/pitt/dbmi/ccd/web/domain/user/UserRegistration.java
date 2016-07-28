/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.domain.user;

import javax.validation.constraints.AssertTrue;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * Feb 23, 2016 3:52:56 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UserRegistration {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email")
    private String username;

    @Length(min = 5, max = 25, message = "Please enter a password (5-25 chars).")
    private String password;

    @Length(min = 5, max = 25, message = "Please reenter password.")
    private String confirmPassword;

    @AssertTrue(message = "You must agree to the terms and conditions.")
    private boolean agree;

    private String firstName;

    private String lastName;

    public UserRegistration() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        if (!(this.password == null || this.password.equals(this.confirmPassword))) {
            this.confirmPassword = "";
        }
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        if (!(this.confirmPassword == null || this.confirmPassword.equals(this.password))) {
            this.password = "";
        }
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
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

}
