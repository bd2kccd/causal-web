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

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public final class ForbiddenException extends RuntimeException {

    private static final String MESSAGE = "User %s forbidden from accessing %s with method %s";

    private final String username;
    private final String path;
    private final String method;

    public ForbiddenException(UserAccount requester, HttpServletRequest request) {
        super();
        this.username = requester.getUsername();
        this.path = request.getRequestURI();
        this.method = request.getMethod();
    }

    @Override
    public String getMessage() {
        return String.format(MESSAGE, username, path, method);
    }
}
