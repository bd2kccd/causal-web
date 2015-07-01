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
package edu.pitt.dbmi.ccd.web.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * Jun 23, 2015 10:10:17 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
public class ExceptionHandlingController {

    @RequestMapping(value = "/404", method = {RequestMethod.GET, RequestMethod.POST})
    public String showPageNotFound() {
        return "404";
    }

    @RequestMapping(value = "/500", method = {RequestMethod.GET, RequestMethod.POST})
    public String showInternalServerError() {
        return "500";
    }

    @RequestMapping(value = "/401", method = {RequestMethod.GET, RequestMethod.POST})
    public String showUnauthorizedAccess() {
        return "unauthorized";
    }

}
