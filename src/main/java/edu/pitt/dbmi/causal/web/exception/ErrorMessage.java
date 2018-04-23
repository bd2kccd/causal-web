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
package edu.pitt.dbmi.ccd.annotations.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public final class ErrorMessage {

    private final Date timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    /**
     * Constructor
     *
     * @param http http status
     * @param message exception message
     * @param req http servlet request
     * @return ErrorMessage with current timestamp, status and exception from
     * HttpStatus, message, and path from HttpServletRequest
     */
    public ErrorMessage(HttpStatus http, String message, HttpServletRequest req) {
        this.timestamp = new Date();
        this.status = http.value();
        this.error = http.getReasonPhrase();
        this.message = message;
        this.path = req.getRequestURI();
    }

    /**
     * Constructor
     *
     * @param timestamp timestamp of exception
     * @param status HTTP status code
     * @param error HTTP exception
     * @param message exception message (nullable)
     * @param path request path
     * @return new exception
     */
    public ErrorMessage(Date timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    @JsonInclude(Include.NON_NULL)
    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }
}
