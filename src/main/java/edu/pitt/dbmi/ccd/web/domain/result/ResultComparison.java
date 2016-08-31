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
package edu.pitt.dbmi.ccd.web.domain.result;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * Sep 8, 2015 2:46:34 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultComparison {

    @XmlElementWrapper(name = "files")
    @XmlElement(name = "file")
    private List<ComparedFile> comparedFiles;

    @XmlElementWrapper(name = "comparisonData")
    @XmlElement(name = "data")
    private List<ResultComparisonData> comparisonData;

    public ResultComparison() {
    }

    public ResultComparison(List<ComparedFile> comparedFiles, List<ResultComparisonData> comparisonData) {
        this.comparedFiles = comparedFiles;
        this.comparisonData = comparisonData;
    }

    public List<ComparedFile> getComparedFiles() {
        return comparedFiles;
    }

    public void setComparedFiles(List<ComparedFile> comparedFiles) {
        this.comparedFiles = comparedFiles;
    }

    public List<ResultComparisonData> getComparisonData() {
        return comparisonData;
    }

    public void setComparisonData(List<ResultComparisonData> comparisonData) {
        this.comparisonData = comparisonData;
    }

}
