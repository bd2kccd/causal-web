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
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * Jun 1, 2016 6:09:11 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordReset {

    @NotBlank
    private String activationKey;

    @Length(min = 5, max = 25, message = "Please enter a password (5-25 chars).")
    private String password;

    @Length(min = 5, max = 25, message = "Please reenter password.")
    private String confirmPassword;

    public PasswordReset() {
    }

    public PasswordReset(String activationKey) {
        this.activationKey = activationKey;
    }

    @Override
    public String toString() {
        return "PasswordReset{" + "activationKey=" + activationKey + ", password=" + password + ", confirmPassword=" + confirmPassword + '}';
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
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

}
