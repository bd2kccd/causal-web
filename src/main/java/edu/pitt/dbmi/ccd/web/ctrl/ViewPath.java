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

    public static final String MESSAGE_VIEW = "message";
    public static final String MESSAGE = "/message";
    public static final String REDIRECT_MESSAGE = "redirect:/message";

    public static final String LOGIN = "/login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String LOGOUT = "/secured/logout";

    public static final String HOME_VIEW = "home";
    public static final String HOME = "/secured/home";
    public static final String REDIRECT_HOME = "redirect:/secured/home";

    public static final String USER_PASSWORD_RESET_VIEW = "user/account/recovery/password_reset";

    public static final String TERMS_VIEW = "terms";

    public static final String USER_PROFILE_VIEW = "user/account/userProfile";
    public static final String REDIRECT_USER_PROFILE = "redirect:/secured/user/profile";

    public static final String FILE_UPLOAD_VIEW = "file/fileUpload";
    public static final String FILE_LIST_VIEW = "file/fileList";

    public static final String REDIRECT_NEW_UPLOAD = "redirect:/secured/file/upload/new";
    public static final String REDIRECT_DATA_INPUT = "redirect:/secured/file/input/data";
    public static final String REDIRECT_VARIABLE_INPUT = "redirect:/secured/file/input/variable";
    public static final String REDIRECT_PRIOR_KNOWLEDGE_INPUT = "redirect:/secured/file/input/prior";

    public static final String REDIRECT_ALGO_RESULT_FILE = "redirect:/secured/file/output/algorithm/result/run";

    public static final String CATEGORIZE = "categorize";
    public static final String FILE_INFO_VIEW = "file/fileInfo";

    public static final String INFO = "info";
    public static final String INFO_CATEGORIZE = "info/categorize";

    public static final String RESULT_LIST_VIEW = "result/algorithm/run/resultList";
    public static final String RESULT_INFO_VIEW = "result/algorithm/run/algoResultInfo";
    public static final String D3_GRAPH_VIEW = "graph/d3graph";

}
