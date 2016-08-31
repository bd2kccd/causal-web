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
package edu.pitt.dbmi.ccd.web.domain.account;

import org.hibernate.validator.constraints.Length;

/**
 *
 * Jun 30, 2016 5:14:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordChange {

    @Length(min = 5, max = 25, message = "Please enter your password.")
    private String currentPassword;

    @Length(min = 5, max = 25, message = "Please enter your new password.")
    private String newPassword;

    @Length(min = 5, max = 25, message = "Please reenter your new password.")
    private String newConfirmPassword;

    public PasswordChange() {
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
