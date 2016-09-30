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
 * Apr 21, 2016 11:43:29 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FgsContinuousRunInfo extends AlgorithmRunInfo {

    // algorithm parameters
    protected int maxDegree;
    protected double penaltyDiscount;
    protected boolean faithfulnessAssumed;

    // data validation
    protected boolean nonZeroVarianceValidation;
    protected boolean uniqueVarNameValidation;

    public FgsContinuousRunInfo() {
    }

    @Override
    public String toString() {
        return "FgsContinuousRunInfo{" + "maxDegree=" + maxDegree + ", penaltyDiscount=" + penaltyDiscount + ", faithfulnessAssumed=" + faithfulnessAssumed + ", nonZeroVarianceValidation=" + nonZeroVarianceValidation + ", uniqueVarNameValidation=" + uniqueVarNameValidation + '}';
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
        this.penaltyDiscount = penaltyDiscount;
    }

    public boolean isFaithfulnessAssumed() {
        return faithfulnessAssumed;
    }

    public void setFaithfulnessAssumed(boolean faithfulnessAssumed) {
        this.faithfulnessAssumed = faithfulnessAssumed;
    }

    public boolean isNonZeroVarianceValidation() {
        return nonZeroVarianceValidation;
    }

    public void setNonZeroVarianceValidation(boolean nonZeroVarianceValidation) {
        this.nonZeroVarianceValidation = nonZeroVarianceValidation;
    }

    public boolean isUniqueVarNameValidation() {
        return uniqueVarNameValidation;
    }

    public void setUniqueVarNameValidation(boolean uniqueVarNameValidation) {
        this.uniqueVarNameValidation = uniqueVarNameValidation;
    }

}
