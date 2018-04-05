/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Oct 4, 2016 4:21:15 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UriTool {

    private UriTool() {
    }

    public static UriComponentsBuilder buildURI(HttpServletRequest req) {
        String uri = req.getRequestURL().toString().replace(req.getRequestURI(), "");
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().uri(URI.create(uri));

        String contextPath = req.getContextPath();
        if (!(contextPath == null || contextPath.isEmpty())) {
            uriBuilder = uriBuilder.path(contextPath);
        }

        return uriBuilder;
    }

    public static String ipAddressToHostName(String ipAddress) {
        String hostName;

        try {
            hostName = InetAddress.getByName(ipAddress).getHostName();
        } catch (UnknownHostException exception) {
            hostName = "unknown";
        }

        return hostName;
    }

}
