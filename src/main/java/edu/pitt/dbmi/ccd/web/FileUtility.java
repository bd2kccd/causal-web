/*
 * Copyright (C) 2015 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * May 18, 2015 1:05:35 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileUtility {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private FileUtility() {
    }

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String humanReadableSize(long fileSize, boolean si) {
        int unit = si ? 1000 : 1024;
        if (fileSize < unit) {
            return fileSize + " B";
        }
        int exp = (int) (Math.log(fileSize) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

        return String.format("%.2f %sB", fileSize / Math.pow(unit, exp), pre);
    }

}
