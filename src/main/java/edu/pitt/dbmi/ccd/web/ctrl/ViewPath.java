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
package edu.pitt.dbmi.ccd.web.ctrl;

/**
 *
 * Aug 22, 2016 7:17:37 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public interface ViewPath {

    public static final String TERMS_VIEW = "terms";

    public static final String LOGIN = "login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String LOGOUT = "/secured/logout";

    public static final String HOME_VIEW = "home";
    public static final String HOME = "/secured/home";
    public static final String REDIRECT_HOME = "redirect:/secured/home";

    public static final String MESSAGE = "message";
    public static final String REDIRECT_MESSAGE = "redirect:/message";
    public static final String MESSAGE_VIEW = "message";

    public static final String PASSWORD_RESET_VIEW = "user/account/passwordReset";

    public static final String USER_PROFILE_VIEW = "user/account/userProfileChange";
    public static final String REDIRECT_USER_PROFILE = "redirect:/secured/user/account/profile";

    public static final String FILE_UPLOAD_VIEW = "file/fileUpload";

    public static final String UNCATEGORIZED_FILE_VIEW = "file/uncategorizedFile";
    public static final String REDIRECT_UNCATEGORIZED_FILE = "redirect:/secured/file/uncategorized";

    public static final String CATEGORIZED_FILE_VIEW = "file/categorizedFile";

    public static final String FILE_LIST_VIEW = "file/fileList";
    public static final String REDIRECT_TETRAD_DATA_FILE = "redirect:/secured/file/mgmt/list/tetrad-data";

}
