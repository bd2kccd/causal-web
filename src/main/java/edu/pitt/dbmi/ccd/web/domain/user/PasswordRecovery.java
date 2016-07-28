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

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * May 30, 2016 3:09:48 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordRecovery {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid e-mail.")
    private String usernameRecover;

    public PasswordRecovery() {
    }

    public String getUsernameRecover() {
        return usernameRecover;
    }

    public void setUsernameRecover(String usernameRecover) {
        this.usernameRecover = usernameRecover;
    }

}
