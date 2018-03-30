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
package edu.pitt.dbmi.causal.web.model;

/**
 *
 * Mar 26, 2018 2:07:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class ParamOption extends Option {

    protected boolean numeric;

    protected boolean aBoolean;

    protected String defaultVal;

    protected String minVal;

    protected String maxVal;

    public ParamOption(String value, String text) {
        super(value, text);
    }

    @Override
    public String toString() {
        return "ParamOption{" + "value=" + value + ", text=" + text + ", description=" + description + "numeric=" + numeric + ", aBoolean=" + aBoolean + ", defaultVal=" + defaultVal + ", minVal=" + minVal + ", maxVal=" + maxVal + '}';
    }

    public boolean isNumeric() {
        return numeric;
    }

    public void setNumeric(boolean numeric) {
        this.numeric = numeric;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    public String getMinVal() {
        return minVal;
    }

    public void setMinVal(String minVal) {
        this.minVal = minVal;
    }

    public String getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(String maxVal) {
        this.maxVal = maxVal;
    }

}
