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
package edu.pitt.dbmi.ccd.web.model.algo;

/**
 *
 * May 27, 2017 9:08:20 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FGEScAlgoOpt extends CommonFGESAlgoOpt {

    protected double penaltyDiscount = 2.0;
    protected double structurePrior = 1.0;

    protected boolean skipUniqueVarName = false;
    protected boolean skipNonZeroVariance = false;

    public FGEScAlgoOpt() {
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
        this.penaltyDiscount = penaltyDiscount;
    }

    public double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public boolean isSkipUniqueVarName() {
        return skipUniqueVarName;
    }

    public void setSkipUniqueVarName(boolean skipUniqueVarName) {
        this.skipUniqueVarName = skipUniqueVarName;
    }

    public boolean isSkipNonZeroVariance() {
        return skipNonZeroVariance;
    }

    public void setSkipNonZeroVariance(boolean skipNonZeroVariance) {
        this.skipNonZeroVariance = skipNonZeroVariance;
    }

}
