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

    public static final String LOGIN = "login";
    public static final String LOGIN_VIEW = "login";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String HOME = "home";
    public static final String HOME_VIEW = "home";
    public static final String REDIRECT_HOME = "redirect:/home";

    public static final String SETUP = "setup";
    public static final String SETUP_VIEW = "user/registration/setup";
    public static final String REDIRECT_SETUP = "redirect:/user/registration/setup";

    public static final String FILE_INFO = "fileInfo";
    public static final String FILE_INFO_VIEW = "fs/fileInfo";

    public static final String DIR_BROWSER = "dirBrowser";
    public static final String DIR_BROWSER_VIEW = "fs/dirBrowser";

    public static final String USER_PROFILE = "userProfile";
    public static final String USER_PROFILE_VIEW = "user/userProfile";
    public static final String REDIRECT_USER_PROFILE = "redirect:/user/profile";

    public static final String DATA_SUMMARY = "dataSummary";
    public static final String DATA_SUMMARY_VIEW = "data/dataSummary";

    public static final String DATA_UPLOAD_VIEW = "data/dataUpload";

    public static final String DATASET_VIEW = "data/dataset";

    public static final String DATA_VIEW = "data";
    public static final String REDIRECT_DATA = "redirect:/data";

    public static final String PC_STABLE_VIEW = "algorithm/pcStable";

    public static final String GES_VIEW = "algorithm/ges";

    public static final String ALGO_RUN_CONFIRM_VIEW = "algorithm/algoRunConfirm";

    public static final String ALGORITHM_RESULTS = "algorithm/results";
    public static final String ALGORITHM_RESULTS_VIEW = "algorithm/algorithmResults";
    public static final String REDIRECT_ALGORITHM_RESULTS = "redirect:/algorithm/results";

    public static final String ALGO_RESULT_COMPARE_VIEW = "algorithm/algoResultComparison";
    public static final String REDIRECT_ALGO_RESULT_COMPARE_VIEW = "redirect:/algorithm/result/comparison";
    public static final String ALGO_RESULT_COMPARISON_TABLE_VIEW = "algorithm/algorithmCompareTable";

    public static final String ALGORITHM_RESULT_ERROR_VIEW = "algorithm/resultError";

    public static final String PLOT = "plot";
    public static final String PLOT_VIEW = "algorithm/plot";

    public static final String D3_GRAPH = "d3graph";
    public static final String D3_GRAPH_VIEW = "algorithm/d3graph";

    public static final String JOB_QUEUE = "jobQueue";
    public static final String REDIRECT_JOB_QUEUE = "redirect:/jobQueue";

}
