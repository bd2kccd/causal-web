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
package edu.pitt.dbmi.ccd.web.domain.account;

import javax.validation.constraints.AssertTrue;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * Apr 17, 2017 2:23:44 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UserRegistrationForm {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter your email.")
    private String registerEmail;

    @Length(min = 4, max = 10, message = "Please enter a password (4-10 chars).")
    private String registerPassword;

    @Length(min = 4, max = 10, message = "Please re-enter your password.")
    private String confirmRegisterPassword;

    @AssertTrue(message = "You must agree to the terms and conditions.")
    private boolean agree;

    private String firstName;

    private String middleName;

    private String lastName;

    public UserRegistrationForm() {
    }

    public UserRegistrationForm(boolean agree) {
        this.agree = agree;
    }

    @Override
    public String toString() {
        return "UserRegistrationForm{"
                + "registerEmail=" + registerEmail
                + ", registerPassword=" + registerPassword
                + ", confirmRegisterPassword=" + confirmRegisterPassword
                + ", agree=" + agree
                + ", firstName=" + firstName
                + ", middleName=" + middleName
                + ", lastName=" + lastName + '}';
    }

    public String getRegisterEmail() {
        return registerEmail;
    }

    public void setRegisterEmail(String registerEmail) {
        this.registerEmail = registerEmail;
    }

    public String getRegisterPassword() {
        return registerPassword;
    }

    public void setRegisterPassword(String registerPassword) {
        this.registerPassword = registerPassword;
        if (!(this.registerPassword == null || this.registerPassword.equals(this.confirmRegisterPassword))) {
            this.confirmRegisterPassword = "";
        }
    }

    public String getConfirmRegisterPassword() {
        return confirmRegisterPassword;
    }

    public void setConfirmRegisterPassword(String confirmRegisterPassword) {
        this.confirmRegisterPassword = confirmRegisterPassword;
        if (!(this.confirmRegisterPassword == null || this.confirmRegisterPassword.equals(this.registerPassword))) {
            this.registerPassword = "";
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

}
