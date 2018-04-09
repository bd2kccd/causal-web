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
package edu.pitt.dbmi.causal.web.tetrad;

import edu.cmu.tetrad.algcomparison.utils.TakesIndependenceWrapper;
import edu.cmu.tetrad.algcomparison.utils.UsesScoreWrapper;
import edu.cmu.tetrad.annotation.Algorithm;
import edu.cmu.tetrad.annotation.AnnotatedClass;
import java.io.Serializable;

/**
 *
 * Mar 13, 2018 3:23:03 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradAlgorithm implements Serializable, Comparable<TetradAlgorithm> {

    private static final long serialVersionUID = -5344339492029900326L;

    private final AnnotatedClass<Algorithm> algorithm;
    private final boolean requiredScore;
    private final boolean requiredTest;

    public TetradAlgorithm(AnnotatedClass<Algorithm> algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm annotation cannot be null.");
        }

        this.algorithm = algorithm;
        this.requiredScore = UsesScoreWrapper.class.isAssignableFrom(algorithm.getClazz());
        this.requiredTest = TakesIndependenceWrapper.class.isAssignableFrom(algorithm.getClazz());
    }

    @Override
    public int compareTo(TetradAlgorithm other) {
        return algorithm.getAnnotation().name().compareTo(other.algorithm.getAnnotation().name());
    }

    @Override
    public String toString() {
        return algorithm.getAnnotation().name();
    }

    public AnnotatedClass<Algorithm> getAlgorithm() {
        return algorithm;
    }

    public boolean isRequiredScore() {
        return requiredScore;
    }

    public boolean isRequiredTest() {
        return requiredTest;
    }

}