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

    public static final String DATASET = "--dataset";
    public static final String DATATYPE = "--data-type";
    public static final String INDEPENDENCE_TEST = "--test";
    public static final String SCORE = "--score";

    public static final String KNOWLEDGE = "--knowledge";
    public static final String EXCLUDE_VARS = "--exclude-var";
    public static final String DELIMITER = "--delimiter";
    public static final String NUM_DISCRETE_CATEGORIES = "--num-discrete-categories";
    public static final String VERBOSE = "--verbose";
    public static final String THREAD = "--thread";
    public static final String JSON = "--json";
    public static final String OUT = "--out";
    public static final String OUTPUT_PREFIX = "--prefix";
    public static final String SKIP_LATEST = "--skip-latest";

    public static final String PENALTY_DISCOUNT = "--penaltyDiscount";
    public static final String MAX_DEGREE = "--maxDegree";
    public static final String MAX_PATH_LENGTH = "--maxPathLength";
    public static final String FAITHFULNESS_ASSUMED = "--faithfulnessAssumed";
    public static final String SYMMETRIC_FIRST_STEP = "--symmetricFirstStep";
    public static final String ALPHA = "--alpha";
    public static final String STRUCTURE_PRIOR = "--structurePrior";
    public static final String SAMPLE_PRIOR = "--samplePrior";
    public static final String COMPLETE_RULE_SET_USED = "--completeRuleSetUsed";

    public static final String NUM_CATEGORIES_TO_DISCRETIZE = "--numCategories";

    public static final String DISCRETIZE = "--discretize";

    public static final String SKIP_VALIDATION = "--skip-validation";

}
