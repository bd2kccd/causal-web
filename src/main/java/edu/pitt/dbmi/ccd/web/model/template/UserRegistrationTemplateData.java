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
package edu.pitt.dbmi.ccd.web.model.template;

import java.net.URI;
import java.util.Date;

/**
 *
 * Apr 19, 2017 12:30:35 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UserRegistrationTemplateData {

    private String email;
    private Date registrationDate;
    private String registrationLocation;
    private URI activationLink;

    public UserRegistrationTemplateData() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRegistrationLocation() {
        return registrationLocation;
    }

    public void setRegistrationLocation(String registrationLocation) {
        this.registrationLocation = registrationLocation;
    }

    public URI getActivationLink() {
        return activationLink;
    }

    public void setActivationLink(URI activationLink) {
        this.activationLink = activationLink;
    }

}
