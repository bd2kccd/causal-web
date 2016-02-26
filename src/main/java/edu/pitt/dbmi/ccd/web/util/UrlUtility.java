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
package edu.pitt.dbmi.ccd.web.util;

import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Feb 18, 2016 12:18:13 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class UrlUtility {

    private UrlUtility() {
    }

    /**
     * Implementation of MySQL INET_NTOA function. Given a numeric IPv4 network
     * address in network byte order, returns the dotted-quad string
     * representation of the address as a nonbinary string in the connection
     * character set.
     * http://dev.mysql.com/doc/refman/5.6/en/miscellaneous-functions.html#function_inet-ntoa
     *
     * @param host
     * @return
     * @throws UnknownHostException when the IP address of a host could not be
     * determined
     */
    public static long InetNTOA(String host) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(host);
        byte[] address = inetAddress.getAddress();
        double base = 256.0;
        double power = address.length;
        long result = 0;
        for (byte b : address) {
            result += ((b & 0xFF) % base * Math.pow(base, --power));
        }

        return result;
    }

    /**
     * Implementation of MySQL INET_ATON function. Given the dotted-quad
     * representation of an IPv4 network address as a string, returns an integer
     * that represents the numeric value of the address in network byte order
     * (big endian).
     * http://dev.mysql.com/doc/refman/5.6/en/miscellaneous-functions.html#function_inet-aton
     *
     * @param value
     * @return
     * @throws UnknownHostException when he IP address of a host could not be
     * determined
     */
    public static String InetATON(long value) throws UnknownHostException {
        byte[] addr = {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };

        return InetAddress.getByAddress(addr).getHostAddress();
    }

    public static UriComponentsBuilder buildURI(HttpServletRequest request, CcdProperties ccdProperties) {
        String name = ccdProperties.getCallbackServerName();
        String port = ccdProperties.getCallbackServerPort();

        String serverName = (name == null || name.isEmpty()) ? request.getServerName() : name;  // hostname.com
        int serverPort = (port == null || port.isEmpty()) ? request.getServerPort() : Integer.parseInt(port); // 80
        String scheme = (serverPort == 443) ? "https" : "http";  // http or https
        String contextPath = request.getContextPath();  // /ccd

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(serverName);
        if (!(serverPort == 80 || serverPort == 443)) {
            uriBuilder = uriBuilder.port(serverPort);
        }
        if (!(contextPath == null || contextPath.isEmpty())) {
            uriBuilder = uriBuilder.path(contextPath);
        }

        return uriBuilder;
    }

    public static String buildURI(HttpServletRequest request, CcdProperties ccdProperties, String... pathSegments) {
        return buildURI(request, ccdProperties).pathSegment(pathSegments).build().normalize().toString();
    }

}
