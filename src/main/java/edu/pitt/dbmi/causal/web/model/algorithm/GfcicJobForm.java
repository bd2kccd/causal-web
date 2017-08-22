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
package edu.pitt.dbmi.causal.web.model.algorithm;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 *
 * Jul 27, 2017 5:41:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class GfcicJobForm extends CommonGfciJobForm {

    @Min(value = 0, message = "Value must be at least 0.")
    @Max(value = 1, message = "Value must be at most 1.")
    protected double alpha = 0.01;

    @Min(value = 0, message = "Value must be at least 0.")
    protected double penaltyDiscount = 2.0;

    protected boolean ensureUniqueVarName = true;
    protected boolean ensureNonZeroVariance = true;

    public GfcicJobForm() {
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
