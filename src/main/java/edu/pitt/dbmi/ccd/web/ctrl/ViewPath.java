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

/**
 *
 * May 14, 2015 12:40:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public interface ViewPath {

    public static final String LOGIN = "login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String LOGOUT = "logout";

    public static final String HOME = "home";
    public static final String HOME_VIEW = "secured/home";
    public static final String REDIRECT_HOME = "redirect:/secured/home";

    public static final String USER_ACTIVATION_SUCCESS_VIEW = "user/userActivationSuccess";

}
