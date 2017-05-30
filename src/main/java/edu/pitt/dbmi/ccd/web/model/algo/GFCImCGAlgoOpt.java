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
 * May 29, 2017 2:12:45 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class GFCImCGAlgoOpt extends CommonGFCIAlgoOpt {

    protected double alpha = 0.01;
    protected double penaltyDiscount = 2.0;
    protected double structurePrior = 1.0;
    protected int numCategoriesToDiscretize = 3;
    protected int numberOfDiscreteCategories = 4;
    protected boolean discretize = true;

    public GFCImCGAlgoOpt() {
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

    public int getNumberOfDiscreteCategories() {
        return numberOfDiscreteCategories;
    }

    public void setNumberOfDiscreteCategories(int numberOfDiscreteCategories) {
        this.numberOfDiscreteCategories = numberOfDiscreteCategories;
    }

    public boolean isDiscretize() {
        return discretize;
    }

    public void setDiscretize(boolean discretize) {
        this.discretize = discretize;
    }

}
