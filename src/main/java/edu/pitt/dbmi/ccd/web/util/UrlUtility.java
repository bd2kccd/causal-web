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

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServletRequest;

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

    public static String getHostURL(HttpServletRequest request) {
        String scheme = request.getScheme();  // http or https
        String hostName = request.getServerName();  // hostname.com
        int serverPort = request.getServerPort(); // 80

        if ((serverPort == 80) || (serverPort == 443)) {
            return String.format("%s://%s", scheme, hostName);
        } else {
            return String.format("%s://%s:%d", scheme, hostName, serverPort);
        }
    }

    public static String getURL(HttpServletRequest request) {
        String scheme = request.getScheme();  // http or https
        String hostName = request.getServerName();  // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath();  // /ccd

        if ((serverPort == 80) || (serverPort == 443)) {
            return String.format("%s://%s%s", scheme, hostName, contextPath);
        } else {
            return String.format("%s://%s:%d%s", scheme, hostName, serverPort, contextPath);
        }
    }

    public static String buildURL(HttpServletRequest request, String relativePath) {
        String scheme = request.getScheme();  // http or https
        String hostName = request.getServerName();  // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath();  // /ccd

        if (relativePath != null) {
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.replaceFirst("/", "");
            }
        }

        if ((serverPort == 80) || (serverPort == 443)) {
            if (relativePath == null) {
                return String.format("%s://%s%s", scheme, hostName, contextPath);
            } else {
                return String.format("%s://%s%s/%s", scheme, hostName, contextPath, relativePath);
            }
        } else {
            if (relativePath == null) {
                return String.format("%s://%s:%d%s", scheme, hostName, serverPort, contextPath);
            } else {
                return String.format("%s://%s:%d%s/%s", scheme, hostName, serverPort, contextPath, relativePath);
            }
        }
    }

}
