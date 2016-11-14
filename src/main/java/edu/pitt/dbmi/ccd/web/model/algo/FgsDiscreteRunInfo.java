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
package edu.pitt.dbmi.ccd.web.model.algo;

/**
 *
 * Apr 20, 2016 4:53:29 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FgsDiscreteRunInfo extends AlgorithmRunInfo {

    private double structurePrior;
    private double samplePrior;
    private int maxDegree;
    private boolean faithfulnessAssumed;

    // data validation
    protected boolean skipUniqueVarName;
    protected boolean skipCategoryLimit;

    public FgsDiscreteRunInfo() {
    }

    public double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public double getSamplePrior() {
        return samplePrior;
    }

    public void setSamplePrior(double samplePrior) {
        this.samplePrior = samplePrior;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public boolean isFaithfulnessAssumed() {
        return faithfulnessAssumed;
    }

    public void setFaithfulnessAssumed(boolean faithfulnessAssumed) {
        this.faithfulnessAssumed = faithfulnessAssumed;
    }

    public boolean isSkipUniqueVarName() {
        return skipUniqueVarName;
    }

    public void setSkipUniqueVarName(boolean skipUniqueVarName) {
        this.skipUniqueVarName = skipUniqueVarName;
    }

    public boolean isSkipCategoryLimit() {
        return skipCategoryLimit;
    }

    public void setSkipCategoryLimit(boolean skipCategoryLimit) {
        this.skipCategoryLimit = skipCategoryLimit;
    }

}
