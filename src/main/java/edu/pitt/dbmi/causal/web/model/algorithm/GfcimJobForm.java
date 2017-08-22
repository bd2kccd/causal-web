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
 * Jul 28, 2017 2:27:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class GfcimJobForm extends CommonGfciJobForm {

    @Min(value = 0, message = "Value must be at least 0.")
    @Max(value = 1, message = "Value must be at most 1.")
    protected double alpha = 0.01;

    @Min(value = 0, message = "Value must be at least 0.")
    protected double penaltyDiscount = 2.0;

    @Min(value = 1, message = "Value must be at least 1.")
    protected double structurePrior = 1.0;

    @Min(value = 2, message = "Value must be at least 2.")
    protected int numCategoriesToDiscretize = 3;

    @Min(value = 2, message = "Value must be at least 2.")
    protected int numCategories = 4;

    protected boolean discretize = true;

    public GfcimJobForm() {
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

    public double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public int getNumCategoriesToDiscretize() {
        return numCategoriesToDiscretize;
    }

    public void setNumCategoriesToDiscretize(int numCategoriesToDiscretize) {
        this.numCategoriesToDiscretize = numCategoriesToDiscretize;
    }

    public int getNumCategories() {
        return numCategories;
    }

    public void setNumCategories(int numCategories) {
        this.numCategories = numCategories;
    }

    public boolean isDiscretize() {
        return discretize;
    }

    public void setDiscretize(boolean discretize) {
        this.discretize = discretize;
    }

}
