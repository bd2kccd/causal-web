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
package edu.pitt.dbmi.ccd.web.util;

/**
 * This is for temporary used.
 *
 * Oct 26, 2016 3:44:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class CmdOptions {

    private CmdOptions() {
    }

    public static final String DATA = "--data";
    public static final String EXCLUDE_VARS = "--exclude-variables";
    public static final String DELIMITER = "--delimiter";
    public static final String VERBOSE = "--verbose";

    public static final String PENALTY_DISCOUNT = "--penalty-discount";
    public static final String MAX_DEGREE = "--max-degree";
    public static final String MAX_INDEGREE = "--max-indegree";
    public static final String MAX_OUTDEGREE = "--max-outdegree";
    public static final String FAITHFULNESS_ASSUMED = "--faithfulness-assumed";
    public static final String ALPHA = "--alpha";
    public static final String STRUCTURE_PRIOR = "--structure-prior";
    public static final String SAMPLE_PRIOR = "--sample-prior";

    public static final String LATENT = "--latent";
    public static final String AVG_DEGREE = "--avg-degree";
    public static final String CONNECTED = "--connected";

    public static final String MIN_CATEGORIES = "--min-categories";
    public static final String MAX_CATEGORIES = "--max-categories";

    public static final String SKIP_UNIQUE_VAR_NAME = "--skip-unique-var-name";
    public static final String SKIP_NONZERO_VARIANCE = "--skip-nonzero-variance";
    public static final String SKIP_CATEGORY_LIMIT = "--skip-category-limit";

    public static final double PENALTY_DISCOUNT_DEFAULT = 4.0;
    public static final double ALPHA_DEFAULT = 0.01;
    public static final double STRUCTURE_PRIOR_DEFAULT = 1.0;
    public static final double SAMPLE_PRIOR_DEFAULT = 1.0;

    public static final int MAX_DEGREE_DEFAULT = -1;

    public static final boolean FAITHFULNESS_ASSUMED_DEFAULT = true;
    public static final boolean VERBOSE_DEFAULT = true;

    public static final boolean SKIP_NONZERO_VARIANCE_DEFAULT = false;
    public static final boolean SKIP_CATEGORY_LIMIT_DEFAULT = false;
    public static final boolean SKIP_UNIQUE_VAR_NAME_DEFAULT = false;

}
