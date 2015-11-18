/*
 * Copyright (C) 2015 University of Pittsburgh.
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

import java.util.List;

/**
 *
 * Nov 17, 2015 2:03:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class AlgorithmJobRequest {

    private String algorithmName;

    private String algorithmJar;

    private String algorithm;

    private List<String> dataset;

    private List<String> jvmOptions;

    private List<String> parameters;

    public AlgorithmJobRequest() {
    }

    public AlgorithmJobRequest(String algorithmName, String algorithmJar, String algorithm) {
        this.algorithmName = algorithmName;
        this.algorithmJar = algorithmJar;
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return "AlgorithmJobRequest{" + "algorithmName=" + algorithmName + ", algorithmJar=" + algorithmJar + ", algorithm=" + algorithm + ", dataset=" + dataset + ", jvmOptions=" + jvmOptions + ", parameters=" + parameters + '}';
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getAlgorithmJar() {
        return algorithmJar;
    }

    public void setAlgorithmJar(String algorithmJar) {
        this.algorithmJar = algorithmJar;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public List<String> getDataset() {
        return dataset;
    }

    public void setDataset(List<String> dataset) {
        this.dataset = dataset;
    }

    public List<String> getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(List<String> jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

}
