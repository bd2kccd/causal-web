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
package edu.pitt.dbmi.ccd.annotations.links;

import javax.servlet.http.HttpServletRequest;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

/**
 * Interface for defining links for resources
 *
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
@Component
public interface ResourceLinks {

    // links
    static final String SEARCH = "/search";

    // rels
    static final String REL_SEARCH = "search";

    // query templating
    static final String QUERY_START = "{?";
    static final String QUERY_END = "}";
    static final String PAGEABLE = "page,size,sort";

    // query value
    static final String QUERY = "?";
    static final String EQUAL = "=";

    // get link to index
    // Link self();
    // get link to search page
    Link search();

    // get link of requested URL
    default Link getRequestLink(HttpServletRequest request) {
        final StringBuffer url = request.getRequestURL();
        final String query = request.getQueryString();
        if (query == null) {
            return new Link(url.toString(), Link.REL_SELF);
        } else {
            return new Link(url.append("?").append(query).toString(), Link.REL_SELF);
        }
    }

    // create template for link
    default String toTemplate(String link, String query, String... queries) {
        StringBuilder template = new StringBuilder(link).append(QUERY_START).append(query);
        for (String q : queries) {
            template.append("," + q);
        }
        template.append(QUERY_END);
        return template.toString();
    }

    // create link to resource collection by query param
    default String linkToCollection(String link, String query, String value) {
        return new StringBuilder(link).append(QUERY).append(query).append(EQUAL).append(value).toString();
    }
}
