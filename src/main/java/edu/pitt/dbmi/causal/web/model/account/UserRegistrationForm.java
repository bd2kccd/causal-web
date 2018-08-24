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
package edu.pitt.dbmi.causal.web.model.account;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

/**
 *
 * Apr 17, 2017 2:23:44 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UserRegistrationForm {

    private String firstName;

    private String middleName;

    private String lastName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter your email.")
    private String email;

    @Length(min = 4, message = "Please enter a password.")
    private String password;

    @Length(min = 4, message = "Please re-enter the password.")
    private String confirmPassword;

    @AssertTrue(message = "You must agree to the terms and conditions.")
    private boolean agree;

    public UserRegistrationForm() {
    }

    public UserRegistrationForm(boolean agree) {
        this.agree = agree;
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

}
