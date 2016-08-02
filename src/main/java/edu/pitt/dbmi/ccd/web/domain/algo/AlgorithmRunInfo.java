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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 *
 * Jul 29, 2016 5:16:23 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class AlgorithmRunInfo {

    @NotNull
    @Min(1)
    protected Long dataFileId;

    @NotNull
    @Min(0)
    protected Long varFileId;

    @NotNull
    @Min(0)
    protected Long priorFileId;

    public AlgorithmRunInfo() {
    }

    @Override
    public String toString() {
        return "AlgorithmRunInfo{" + "dataFileId=" + dataFileId + ", varFileId=" + varFileId + ", priorFileId=" + priorFileId + '}';
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

    public Long getPriorFileId() {
        return priorFileId;
    }

    public void setPriorFileId(Long priorFileId) {
        this.priorFileId = priorFileId;
    }

}
