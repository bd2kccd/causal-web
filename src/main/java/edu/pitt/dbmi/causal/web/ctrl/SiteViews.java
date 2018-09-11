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
 * Jun 28, 2018 5:50:36 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public final class SiteViews {

    public static final String HTTP_STATUS = "http_status";

    public static final String SHIRO_LOGIN = "shiro_login";
    public static final String AUTH0_LOGIN = "auth0_shiro_login";

    public static final String MESSAGE = "message";

    public static final String HOME = "home";

    public static final String USER_REGISTRATION = "account/user_registration";

    public static final String USER_PASSWORD_RESET = "account/password_reset";
    public static final String PASSWORD_RESET_REQUEST = "account/password_reset_request";

    public static final String USER_ACCOUNT = "account/user_account";

    public static final String FEEDBACK = "feedback";

    public static final String FILE_UPLOAD = "file/file_upload";
    public static final String FILE = "file/file";
    public static final String FILE_DETAIL = "file/file_detail";
    public static final String FILE_CATEGORIZATION = "file/categorization";
    public static final String FILE_RECATEGORIZATION = "file/recategorization";
    public static final String FILEGROUP = "file/filegroup";
    public static final String FILEGROUP_DETAIL = "file/filegroup_detail";
    public static final String FILEGROUP_NEW = "file/filegroup_new";

    public static final String TETRAD = "algorithm/tetrad";

    public static final String JOB_QUEUE = "job/job_queue";
    public static final String TETRAD_JOB_DETAIL = "job/tetrad_job_detail";

    private SiteViews() {
    }

}
