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
package edu.pitt.dbmi.ccd.web.model.result;

/**
 *
 * Sep 8, 2015 2:47:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class ResultComparisonData {

    private int countIndex;

    private String edge;

    private boolean inAll;

    private boolean sameEdgeType;

    public ResultComparisonData() {
    }

    public ResultComparisonData(String edge) {
        this.edge = edge;
    }

    public int getCountIndex() {
        return countIndex;
    }

    public void setCountIndex(int countIndex) {
        this.countIndex = countIndex;
    }

    public String getEdge() {
        return edge;
    }

    public void setEdge(String edge) {
        this.edge = edge;
    }

    public boolean isInAll() {
        return inAll;
    }

    public void setInAll(boolean inAll) {
        this.inAll = inAll;
    }

    public boolean isSameEdgeType() {
        return sameEdgeType;
    }

    public void setSameEdgeType(boolean sameEdgeType) {
        this.sameEdgeType = sameEdgeType;
    }

}
