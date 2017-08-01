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

    public static final String FEEDBACK_VIEW = "feedback";
    public static final String REDIRECT_FEEDBACK_VIEW = "redirect:/secured/feedback";

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

    public static final String FILE_VIEW = "file/file";
    public static final String FILE_UPLOAD_VIEW = "file/fileUpload";

    public static final String FILEGROUP_LIST_VIEW = "file/fileGroupList";
    public static final String REDIRECT_FILEGROUP_LIST = "redirect:/secured/file/group";

    public static final String FILEGROUP_VIEW = "file/fileGroup";
    public static final String REDIRECT_FILEGROUP_VIEW = "redirect:/secured/file/group/new";
    public static final String REDIRECT_UPDATE_FILEGROUP_VIEW = "redirect:/secured/file/group/update?id=";

    public static final String REDIRECT_UNCATEGORIZED_FILE = "redirect:/secured/file/uncategorized";

    public static final String FILE_CATEGORIZE_VIEW = "file/fileCategorize";
    public static final String REDIRECT_FILE_CATEGORIZE = "redirect:/secured/file/categorize?id=";

    public static final String FILE_LIST_VIEW = "file/fileList";
    public static final String REDIRECT_TETRAD_DATA_FILE = "redirect:/secured/file/mgmt/list/tetrad-data";

    public static final String CAUSAL_DISCOVER_VIEW = "algo/causalDiscovery";
    public static final String FGESC_VIEW = "algo/fgesc";
    public static final String FGESD_VIEW = "algo/fgesd";
    public static final String FGESM_VIEW = "algo/fgesm";
    public static final String GFCIC_VIEW = "algo/gfcic";
    public static final String GFCID_VIEW = "algo/gfcid";
    public static final String GFCIM_VIEW = "algo/gfcim";
    public static final String REDIRECT_FGES_VIEW = "redirect:/secured/algorithm/causal-discover/fges/";
    public static final String REDIRECT_GFCI_VIEW = "redirect:/secured/algorithm/causal-discover/gfci/";

}
