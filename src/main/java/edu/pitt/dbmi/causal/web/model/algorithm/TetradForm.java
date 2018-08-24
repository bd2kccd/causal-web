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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 *
 * Mar 5, 2018 2:11:53 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradForm {

    @NotBlank(message = "Job name is required.")
    private String name;

    private String description;

    @NotNull(message = "Variable type is required.")
    private Long varTypeId;

    @NotNull(message = "Data file ID is required.")
    private Long dataFileId;

    private Long varFileId;

    private Long knwlFileId;

    private boolean singleDataFile;

    private String algoType;

    @NotBlank(message = "Algorithm is required.")
    private String algorithm;

    private String score;

    private String test;

    private int jvmMaxMem = 1;

    public TetradForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getVarTypeId() {
        return varTypeId;
    }

    public void setVarTypeId(Long varTypeId) {
        this.varTypeId = varTypeId;
    }

    public Long getDataFileId() {
        return dataFileId;
    }

    public void setDataFileId(Long dataFileId) {
        this.dataFileId = dataFileId;
    }

    public Long getVarFileId() {
        return varFileId;
    }

    public void setVarFileId(Long varFileId) {
        this.varFileId = varFileId;
    }

    public Long getKnwlFileId() {
        return knwlFileId;
    }

    public void setKnwlFileId(Long knwlFileId) {
        this.knwlFileId = knwlFileId;
    }

    public boolean isSingleDataFile() {
        return singleDataFile;
    }

    public void setSingleDataFile(boolean singleDataFile) {
        this.singleDataFile = singleDataFile;
    }

    public String getAlgoType() {
        return algoType;
    }

    public void setAlgoType(String algoType) {
        this.algoType = algoType;
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

    public int getJvmMaxMem() {
        return jvmMaxMem;
    }

    public void setJvmMaxMem(int jvmMaxMem) {
        this.jvmMaxMem = jvmMaxMem;
    }

}
