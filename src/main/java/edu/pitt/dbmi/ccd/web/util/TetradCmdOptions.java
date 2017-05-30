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
package edu.pitt.dbmi.ccd.web.util;

/**
 *
 * May 28, 2017 11:35:09 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public interface TetradCmdOptions {

    public static final String DATA = "--data";

    public static final String KNOWLEDGE = "--knowledge";
    public static final String EXCLUDE_VARS = "--exclude-variables";
    public static final String DELIMITER = "--delimiter";
    public static final String NUM_DISCRETE_CATEGORIES = "--num-discrete-categories";
    public static final String VERBOSE = "--verbose";
    public static final String THREAD = "--thread";
    public static final String JSON = "--json";
    public static final String TETRAD_GRAPH_JSON = "--tetrad-graph-json";
    public static final String OUT = "--out";
    public static final String OUTPUT_PREFIX = "--output-prefix";
    public static final String NO_VALIDATION_OUTPUT = "--no-validation-output";
    public static final String HELP = "--help";
    public static final String SKIP_LATEST = "--skip-latest";

    public static final String PENALTY_DISCOUNT = "--penalty-discount";
    public static final String MAX_DEGREE = "--max-degree";
    public static final String MAX_INDEGREE = "--max-indegree";
    public static final String MAX_OUTDEGREE = "--max-outdegree";
    public static final String MAX_PATH_LENGTH = "--max-path-length";
    public static final String FAITHFULNESS_ASSUMED = "--faithfulness-assumed";
    public static final String SYMMETRIC_FIRST_STEP = "--symmetric-first-step";
    public static final String ALPHA = "--alpha";
    public static final String STRUCTURE_PRIOR = "--structure-prior";
    public static final String SAMPLE_PRIOR = "--sample-prior";
    public static final String COMPLETE_RULE_SET_USED = "--use-complete-rule-set";

    public static final String LATENT = "--latent";
    public static final String AVG_DEGREE = "--avg-degree";
    public static final String CONNECTED = "--connected";

    public static final String MIN_CATEGORIES = "--min-categories";
    public static final String MAX_CATEGORIES = "--max-categories";
    public static final String NUM_CATEGORIES_TO_DISCRETIZE = "--num-categories-to-discretize";

    public static final String DISCRETIZE = "--discretize";

    public static final String PERCENT_DISCRETE = "--percent-discrete";

    public static final String SKIP_UNIQUE_VAR_NAME = "--skip-unique-var-name";
    public static final String SKIP_NONZERO_VARIANCE = "--skip-nonzero-variance";
    public static final String SKIP_CATEGORY_LIMIT = "--skip-category-limit";

}
