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
 * Jul 24, 2017 11:01:00 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class CommonFgesJobForm extends TetradJobForm {

    @Min(value = -1, message = "Value must be at least -1.")
    protected int maxDegree = 100;

    protected boolean symmetricFirstStep = false;
    protected boolean faithfulnessAssumed = false;

    public CommonFgesJobForm() {
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public boolean isSymmetricFirstStep() {
        return symmetricFirstStep;
    }

    public void setSymmetricFirstStep(boolean symmetricFirstStep) {
        this.symmetricFirstStep = symmetricFirstStep;
    }

    public boolean isFaithfulnessAssumed() {
        return faithfulnessAssumed;
    }

    public void setFaithfulnessAssumed(boolean faithfulnessAssumed) {
        this.faithfulnessAssumed = faithfulnessAssumed;
    }

}
