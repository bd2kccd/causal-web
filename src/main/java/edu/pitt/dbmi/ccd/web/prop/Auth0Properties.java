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
package edu.pitt.dbmi.ccd.web.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * Feb 15, 2016 5:36:40 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@Profile("auth0")
@ConfigurationProperties("auth0")
public class Auth0Properties {

    private String clientId;
    private String clientSecret;
    private String domain;

    private String redirectOnAuthenticationError;
    private String redirectOnSuccess;
    private String redirectOnError;

    private String callbackUrlMapping;

    public Auth0Properties() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRedirectOnAuthenticationError() {
        return redirectOnAuthenticationError;
    }

    public void setRedirectOnAuthenticationError(String redirectOnAuthenticationError) {
        this.redirectOnAuthenticationError = redirectOnAuthenticationError;
    }

    public String getRedirectOnSuccess() {
        return redirectOnSuccess;
    }

    public void setRedirectOnSuccess(String redirectOnSuccess) {
        this.redirectOnSuccess = redirectOnSuccess;
    }

    public String getRedirectOnError() {
        return redirectOnError;
    }

    public void setRedirectOnError(String redirectOnError) {
        this.redirectOnError = redirectOnError;
    }

    public String getCallbackUrlMapping() {
        return callbackUrlMapping;
    }

    public void setCallbackUrlMapping(String callbackUrlMapping) {
        this.callbackUrlMapping = callbackUrlMapping;
    }

}
