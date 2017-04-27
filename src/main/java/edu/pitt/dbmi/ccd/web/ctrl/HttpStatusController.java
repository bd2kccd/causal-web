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
package edu.pitt.dbmi.ccd.web.ctrl;

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

    private static final String ERROR_MSG_TITLE = "Oh, snap!";

    @RequestMapping(value = "/401")
    public String showUnauthorizedAccess(final Model model) {
        model.addAttribute("pageTitle", "Unauthorized Access");
        model.addAttribute("msgTitle", ERROR_MSG_TITLE);
        model.addAttribute("msg", "Sorry, you need to sign in to view this page.");

        return "status/htmlStatus";
    }

    @RequestMapping(value = "/404")
    public String showPageNotFound(final Model model) {
        model.addAttribute("pageTitle", "Page Not Found");
        model.addAttribute("msgTitle", ERROR_MSG_TITLE);
        model.addAttribute("msg", "Sorry, but the page you were trying to view does not exist.");

        return "status/htmlStatus";
    }

    @RequestMapping(value = "/500")
    public String showInternalServerError(final Model model) {
        model.addAttribute("pageTitle", "Internal Server Error");
        model.addAttribute("msgTitle", ERROR_MSG_TITLE);
        model.addAttribute("msg", "Sorry, we are currently experiencing an internal issue.");

        return "status/htmlStatus";
    }

}
