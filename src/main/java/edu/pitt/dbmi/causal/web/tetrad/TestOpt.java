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

import edu.cmu.tetrad.annotation.AnnotatedClass;
import edu.cmu.tetrad.annotation.TestOfIndependence;
import edu.cmu.tetrad.data.DataType;
import java.io.Serializable;

/**
 *
 * Mar 14, 2018 5:50:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TestOpt implements Serializable, Comparable<TestOpt> {

    private static final long serialVersionUID = 4835759464841756862L;

    private final AnnotatedClass<TestOfIndependence> test;

    public TestOpt(AnnotatedClass<TestOfIndependence> test) {
        this.test = test;
    }

    @Override
    public int compareTo(TestOpt other) {
        return test.getAnnotation().name().compareTo(other.test.getAnnotation().name());
    }

    @Override
    public String toString() {
        return test.getAnnotation().name();
    }

    public AnnotatedClass<TestOfIndependence> getTest() {
        return test;
    }

    private String getProperty(DataType dataType) {
        switch (dataType) {
            case Continuous:
                return "datatype.continuous.test.default";
            case Discrete:
                return "datatype.discrete.test.default";
            case Mixed:
                return "datatype.mixed.test.default";
            default:
                return null;
        }
    }

}
