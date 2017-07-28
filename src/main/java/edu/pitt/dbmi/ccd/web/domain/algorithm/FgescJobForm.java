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
package edu.pitt.dbmi.ccd.web.domain.algorithm;

import javax.validation.constraints.Min;

/**
 *
 * Jul 20, 2017 6:02:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FgescJobForm extends CommonFgesJobForm {

    @Min(value = 0, message = "Value must be at least 0.")
    protected double penaltyDiscount = 2.0;

    @Min(value = 1, message = "Value must be at least 1.")
    protected double structurePrior = 1.0;

    protected boolean ensureUniqueVarName = true;
    protected boolean ensureNonZeroVariance = true;

    public FgescJobForm() {
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

    public boolean isEnsureUniqueVarName() {
        return ensureUniqueVarName;
    }

    public void setEnsureUniqueVarName(boolean ensureUniqueVarName) {
        this.ensureUniqueVarName = ensureUniqueVarName;
    }

    public boolean isEnsureNonZeroVariance() {
        return ensureNonZeroVariance;
    }

    public void setEnsureNonZeroVariance(boolean ensureNonZeroVariance) {
        this.ensureNonZeroVariance = ensureNonZeroVariance;
    }

}
