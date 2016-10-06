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
 * Sep 28, 2016 7:19:40 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 *
 */
public class GfciContinuousRunInfo extends AlgorithmRunInfo {

    // algorithm parameters
    protected double alpha;
    protected int maxInDegree;
    protected double penaltyDiscount;
    protected boolean faithfulnessAssumed;

    // data validation
    protected boolean nonZeroVarianceValidation;
    protected boolean uniqueVarNameValidation;

    public GfciContinuousRunInfo() {

    }

    @Override
    public String toString() {
        return "FgsContinuousRunInfo{" + "alpha=" + alpha + ", maxInDegree=" + maxInDegree + ", penaltyDiscount=" + penaltyDiscount + ", faithfulnessAssumed=" + faithfulnessAssumed + ", nonZeroVarianceValidation=" + nonZeroVarianceValidation + ", uniqueVarNameValidation=" + uniqueVarNameValidation + '}';
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public int getMaxInDegree() {
        return maxInDegree;
    }

    public void setMaxInDegree(int maxInDegree) {
        this.maxInDegree = maxInDegree;
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
