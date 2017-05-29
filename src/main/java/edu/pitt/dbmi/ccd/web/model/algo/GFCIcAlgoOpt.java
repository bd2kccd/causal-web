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
 * May 28, 2017 9:27:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class GFCIcAlgoOpt extends CommonGFCIAlgoOpt {

    protected double alpha = 0.01;
    protected double penaltyDiscount = 2.0;

    protected boolean skipUniqueVarName = false;
    protected boolean skipNonZeroVariance = false;

    public GFCIcAlgoOpt() {
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
        this.penaltyDiscount = penaltyDiscount;
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
