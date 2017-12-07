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
 * FGES with conditional Gaussian score for mixed variables.
 *
 * May 29, 2017 12:38:08 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FGESmCGAlgoOpt extends CommonFGESAlgoOpt {

    protected int numCategoriesToDiscretize = 3;
    protected int numberOfDiscreteCategories = 4;
    protected double structurePrior = 1.0;
    protected boolean discretize = true;

    public FGESmCGAlgoOpt() {
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

    public double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public boolean isDiscretize() {
        return discretize;
    }

    public void setDiscretize(boolean discretize) {
        this.discretize = discretize;
    }

}
