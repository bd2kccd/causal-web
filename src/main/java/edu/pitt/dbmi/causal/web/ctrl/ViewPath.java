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
 * Feb 7, 2018 3:38:24 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public final class ViewPath {

    public static final String MESSAGE = "message";
    public static final String REDIRECT_MESSAGE = "redirect:/message";
    public static final String MESSAGE_VIEW = "message";

    public static final String LOGIN = "login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String LOGOUT = "/secured/logout";

    public static final String HOME_VIEW = "home";
    public static final String HOME = "/secured/home";
    public static final String REDIRECT_HOME = "redirect:/secured/home";

    public static final String USER_REGISTRATION_VIEW = "account/user_registration";
    public static final String REDIRECT_USER_REGISTRATION = "redirect:/user/account/registration";

    public static final String REDIRECT_AUTH0_USER_REGISTRATION = "redirect:/auth0/user/registration";

    public static final String USER_PROFILE_VIEW = "account/user_profile";
    public static final String REDIRECT_USER_PROFILE = "redirect:/secured/account/profile";

    public static final String FILE_VIEW = "file/file";
    public static final String FILE_UPLOAD_VIEW = "file/file_upload";
    public static final String FILE_LIST_VIEW = "file/file_list";
    public static final String FILE_INFO_VIEW = "file/file_info";
    public static final String REDIRECT_FILE_LIST = "redirect:/secured/file/format/";
    public static final String REDIRECT_FILE_INFO = "redirect:/secured/file/";

    public static final String FILEGROUP_LIST_VIEW = "file/filegroup_list";
    public static final String FILEGROUP_VIEW = "file/filegroup";
    public static final String REDIRECT_FILEGROUP_NEW = "redirect:/secured/file/group/new";
    public static final String REDIRECT_FILEGROUP = "redirect:/secured/file/group/";
    public static final String REDIRECT_FILEGROUP_LIST = "redirect:/secured/file/group";

    private ViewPath() {
    }

}
