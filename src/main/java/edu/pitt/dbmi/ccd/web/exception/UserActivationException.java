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
package edu.pitt.dbmi.ccd.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * Aug 4, 2015 1:42:44 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserActivationException extends RuntimeException {

    private static final long serialVersionUID = -4861092601653641980L;

    public UserActivationException() {
    }

    public UserActivationException(String message) {
        super(message);
    }

    public UserActivationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserActivationException(Throwable cause) {
        super(cause);
    }

    public UserActivationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
