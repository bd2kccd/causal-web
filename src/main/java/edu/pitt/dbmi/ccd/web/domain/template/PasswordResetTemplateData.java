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
package edu.pitt.dbmi.ccd.web.domain.template;

import java.net.URI;

/**
 *
 * Apr 20, 2017 5:10:18 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordResetTemplateData {

    private String username;
    private URI resetPasswordURL;

    public PasswordResetTemplateData() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public URI getResetPasswordURL() {
        return resetPasswordURL;
    }

    public void setResetPasswordURL(URI resetPasswordURL) {
        this.resetPasswordURL = resetPasswordURL;
    }

}
