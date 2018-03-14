/*
 * Copyright (C) 2018 University of Pittsburgh.
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

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * Mar 5, 2018 2:11:53 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradForm {

    @NotNull
    private Long varTypeId;

    @NotNull
    private Long datasetId;

    private boolean multiData;

    @NotEmpty
    private String algorithm;

    private String score;

    private String test;

    public TetradForm() {
    }

    @Override
    public String toString() {
        return "TetradForm{" + "varTypeId=" + varTypeId + ", datasetId=" + datasetId + ", multiData=" + multiData + ", algorithm=" + algorithm + ", score=" + score + ", test=" + test + '}';
    }

    public Long getVarTypeId() {
        return varTypeId;
    }

    public void setVarTypeId(Long varTypeId) {
        this.varTypeId = varTypeId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public boolean isMultiData() {
        return multiData;
    }

    public void setMultiData(boolean multiData) {
        this.multiData = multiData;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

}
