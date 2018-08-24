/*
 * Copyright (C) 2018 University of Pittsburgh.
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

/**
 *
 * Jun 28, 2018 5:52:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public final class SitePaths {

    public static final String LOGIN = "login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String LOGOUT = "secured/logout";

    public static final String MESSAGE = "message";
    public static final String REDIRECT_MESSAGE = "redirect:/message";

    public static final String HOME = "secured/home";
    public static final String REDIRECT_HOME = "redirect:/secured/home";

    public static final String REDIRECT_USER_REGISTRATION = "redirect:/user/account/registration";
    public static final String REDIRECT_AUTH0_USER_REGISTRATION = "redirect:/auth0/user/registration";

    public static final String REDIRECT_PASSWORD_RESET_REQUEST = "redirect:/user/account/password/reset";

    public static final String REDIRECT_USER_ACCOUNT = "redirect:/secured/account";

    public static final String REDIRECT_FEEDBACK = "redirect:/secured/feedback";

    public static final String REDIRECT_FILE = "redirect:/secured/file";
    public static final String REDIRECT_FILE_CATEGORIZATION = "redirect:/secured/file/categorization";
    public static final String REDIRECT_NEW_FILEGROUP = "redirect:/secured/file/group/new";
    public static final String REDIRECT_FILEGROUP = "redirect:/secured/file/group";

    public static final String REDIRECT_TETRAD = "redirect:/secured/algorithm/tetrad";

    public static final String JOB_QUEUE = "secured/job/queue";
    public static final String REDIRECT_JOB_QUEUE = "redirect:/secured/job/queue";

    public static final String REDIRECT_JOB_DETAIL = "redirect:/secured/job/detail";

    private SitePaths() {
    }

}
