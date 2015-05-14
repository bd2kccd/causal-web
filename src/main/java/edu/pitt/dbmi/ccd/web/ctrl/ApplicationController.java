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
 * May 14, 2015 12:39:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
public class ApplicationController implements ViewController {

    public ApplicationController() {
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage() {
        return LOGIN;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage() {
        return showLoginPage();
    }

    @RequestMapping(value = "/404", method = RequestMethod.GET)
    public String showPageNotFound() {
        return "404";
    }

}
