/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.domain.algo;

/**
 *
 * Jul 29, 2016 5:16:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AlgorithmRunInfo {

    protected String dataFileTitle;

    protected String excludeVarFileTitle;

    protected String priorFileTitle;

    protected boolean verbose;

    protected int jvmMaxMem;

    public AlgorithmRunInfo() {
    }

    public String getDataFileTitle() {
        return dataFileTitle;
    }

    public void setDataFileTitle(String dataFileTitle) {
        this.dataFileTitle = dataFileTitle;
    }

    public String getExcludeVarFileTitle() {
        return excludeVarFileTitle;
    }

    public void setExcludeVarFileTitle(String excludeVarFileTitle) {
        this.excludeVarFileTitle = excludeVarFileTitle;
    }

    public String getPriorFileTitle() {
        return priorFileTitle;
    }

    public void setPriorFileTitle(String priorFileTitle) {
        this.priorFileTitle = priorFileTitle;
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
