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
    protected boolean discretize = true;
    protected double structurePrior = 1.0;

    protected int numCategories = 4;

    public GFCImCGAlgoOpt() {
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public boolean isDiscretize() {
        return discretize;
    }

    public void setDiscretize(boolean discretize) {
        this.discretize = discretize;
    }

    public double getStructurePrior() {
        return structurePrior;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public int getNumCategories() {
        return numCategories;
    }

    public void setNumCategories(int numCategories) {
        this.numCategories = numCategories;
    }

}
