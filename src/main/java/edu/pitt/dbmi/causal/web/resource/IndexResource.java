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
package edu.pitt.dbmi.ccd.annotations.resource;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

/**
 * Group entity DTO representation with self link
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public final class IndexResource extends ResourceSupport {

    private final String message;

    /**
     * Constructor
     *
     * @param message message to display
     * @param links (optional) links to include
     * @return index
     */
    public IndexResource(String message, Link... links) {
        this.message = message;
        this.add(links);
    }

    /**
     * Get message
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }
}
