/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.ctrl;

import edu.pitt.dbmi.causal.web.model.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * Apr 7, 2016 3:10:12 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
public class HttpStatusController {

    private static final String STATUS_TITLE = "Oh, snap!";
    private static final String HTTP_STATUS_MODEL = "httpStatus";
    private static final String HTTP_STATUS_VIEW = "http-status";

    private static final HttpStatus BAD_REQ = new HttpStatus("Bad Request", STATUS_TITLE, "Sorry, bad request.");
    private static final HttpStatus UNAUTH_ACCESS = new HttpStatus("Unauthorized Access", STATUS_TITLE, "Sorry, you need to sign in to view this page.");
    private static final HttpStatus PAGE_NOT_FOUND = new HttpStatus("Page Not Found", STATUS_TITLE, "Sorry, but the page you were trying to view does not exist.");
    private static final HttpStatus INTERNAL_SERV_ERR = new HttpStatus("Internal Server Error", STATUS_TITLE, "Sorry, we are currently experiencing an internal issue.");

    @RequestMapping(value = "400")
    public String showBadRequest(final Model model) {
        model.addAttribute(HTTP_STATUS_MODEL, BAD_REQ);

        return HTTP_STATUS_VIEW;
    }

    @RequestMapping(value = "401")
    public String showUnauthorizedAccess(final Model model) {
        model.addAttribute(HTTP_STATUS_MODEL, UNAUTH_ACCESS);

        return HTTP_STATUS_VIEW;
    }

    @RequestMapping(value = "404")
    public String showPageNotFound(final Model model) {
        model.addAttribute(HTTP_STATUS_MODEL, PAGE_NOT_FOUND);

        return HTTP_STATUS_VIEW;
    }

    @RequestMapping(value = "405")
    public String showMethodNotAllowed(final Model model) {
        return showPageNotFound(model);
    }

    @RequestMapping(value = "500")
    public String showInternalServerError(final Model model) {
        model.addAttribute(HTTP_STATUS_MODEL, INTERNAL_SERV_ERR);

        return HTTP_STATUS_VIEW;
    }

}
