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
package edu.pitt.dbmi.ccd.web.bean;

import com.auth0.web.Auth0Config;
import edu.pitt.dbmi.ccd.web.conf.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * Sep 27, 2016 2:55:10 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Component
public class Auth0Helper {

    private final CcdProperties ccdProperties;
    private final Auth0Config auth0Config;

    @Autowired
    public Auth0Helper(CcdProperties ccdProperties, Auth0Config auth0Config) {
        this.ccdProperties = ccdProperties;
        this.auth0Config = auth0Config;
    }

    public String buildLogoutUrl(HttpServletRequest req, String returnToPath) {
        String host = UriTool.buildURI(req, ccdProperties).build().toString();

        return String.format("https://%s/v2/logout?returnTo=%s/%s&client_id=%s",
                auth0Config.getDomain(),
                host,
                returnToPath,
                auth0Config.getClientId());
    }

}
