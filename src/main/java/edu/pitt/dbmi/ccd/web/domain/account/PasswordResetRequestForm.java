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

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * Apr 20, 2017 4:10:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordResetRequestForm {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid e-mail.")
    private String emailToResetPassword;

    public PasswordResetRequestForm() {
    }

    @Override
    public String toString() {
        return "PasswordResetRequestForm{" + "emailToResetPassword=" + emailToResetPassword + '}';
    }

    public String getEmailToResetPassword() {
        return emailToResetPassword;
    }

    public void setEmailToResetPassword(String emailToResetPassword) {
        this.emailToResetPassword = emailToResetPassword;
    }

}
