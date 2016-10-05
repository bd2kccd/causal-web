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

    public static final String LOGIN = "login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String LOGOUT = "logout";

    public static final String HOME = "home";
    public static final String HOME_VIEW = "home";
    public static final String REDIRECT_HOME = "redirect:/home";

    public static final String USER_PROFILE = "userProfile";
    public static final String USER_PROFILE_VIEW = "user/account/userProfile";
    public static final String REDIRECT_USER_PROFILE = "redirect:/user/account/profile";

    public static final String REQUEST_USERNAME_VIEW = "user/account/reqestUsername";
    public static final String REQUEST_ANSWER_VIEW = "user/account/reqestAnswer";
    public static final String REQUEST_NEW_PASSWORD_VIEW = "user/account/reqestNewPassword";
    public static final String REQUEST_PASSWORD_CHANGED_DONE_VIEW = "user/account/requestPwdChangeDone";

    public static final String DATASET_VIEW = "data/dataset";
    public static final String REDIRECT_DATA = "redirect:/data";
    public static final String DATA_SUMMARY_VIEW = "data/dataSummary";
    public static final String DATA_UPLOAD_VIEW = "data/dataUpload";

    public static final String DATASET_PRIOR_VIEW = "data/prior";
    public static final String REDIRECT_DATASET_PRIOR = "redirect:/data/prior";

    public static final String FILE_INFO_VIEW = "fs/fileInfo";

    public static final String ALGORITHM_RESULTS_VIEW = "algorithm/algorithmResults";
    public static final String REDIRECT_ALGORITHM_RESULTS = "redirect:/algorithm/results";
    public static final String ALGORITHM_RESULT_ERROR_VIEW = "algorithm/resultError";
    public static final String PLOT_VIEW = "algorithm/plot";
    public static final String D3_GRAPH_VIEW = "algorithm/d3graph";

    public static final String ALGO_RESULT_COMPARE_VIEW = "algorithm/algoResultComparison";
    public static final String REDIRECT_ALGO_RESULT_COMPARE_VIEW = "redirect:/algorithm/results/comparison";
    public static final String ALGO_RESULT_COMPARISON_TABLE_VIEW = "algorithm/algorithmCompareTable";

    public static final String FGS_VIEW = "algorithm/fgs";
    public static final String FGS_DISCRETE_VIEW = "algorithm/fgsDiscrete";
    public static final String GFCI_VIEW = "algorithm/gfci";

    public static final String JOB_QUEUE = "jobQueue";
    public static final String REDIRECT_JOB_QUEUE = "redirect:/jobQueue";

    public static final String FEEDBACK_VIEW = "feedback";

    public static final String USER_ACTIVATION_SUCCESS_VIEW = "user/userActivationSuccess";

}
