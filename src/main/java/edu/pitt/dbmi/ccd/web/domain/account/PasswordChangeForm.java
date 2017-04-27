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

import org.hibernate.validator.constraints.Length;

/**
 *
 * Apr 21, 2017 7:21:26 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordChangeForm {

    @Length(min = 4, max = 10, message = "Please enter the current password.")
    private String currentPassword;

    @Length(min = 4, max = 10, message = "Please enter a password (4-10 chars).")
    private String newPassword;

    @Length(min = 4, max = 10, message = "Please reenter the password.")
    private String newConfirmPassword;

    public PasswordChangeForm() {
    }

    @Override
    public String toString() {
        return "PasswordChangeForm{"
                + "currentPassword=" + currentPassword
                + ", newPassword=" + newPassword
                + ", newConfirmPassword=" + newConfirmPassword + '}';
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
        if (!(this.newPassword == null || this.newPassword.equals(this.newConfirmPassword))) {
            this.newConfirmPassword = "";
        }
    }

    public String getNewConfirmPassword() {
        return newConfirmPassword;
    }

    public void setNewConfirmPassword(String newConfirmPassword) {
        this.newConfirmPassword = newConfirmPassword;
        if (!(this.newConfirmPassword == null || this.newConfirmPassword.equals(this.newPassword))) {
            this.newPassword = "";
        }
    }

}
