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
package edu.pitt.dbmi.ccd.web.model.algorithm;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * Jul 21, 2017 12:23:34 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class TetradJobForm {

    @NotBlank
    protected String dataset;

    protected String knowledge;

    protected boolean verbose = false;

    @Min(value = 0, message = "Value must be at least 0.")
    @Max(value = 128, message = "Value must be at most 128.")
    protected int jvmMaxMem = 0;

    public TetradJobForm() {
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(String knowledge) {
        this.knowledge = knowledge;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getJvmMaxMem() {
        return jvmMaxMem;
    }

    public void setJvmMaxMem(int jvmMaxMem) {
        this.jvmMaxMem = jvmMaxMem;
    }

}
