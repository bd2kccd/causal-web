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
package edu.pitt.dbmi.ccd.web.prop;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * Jul 14, 2017 2:10:43 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PropertySource("classpath:tetrad.properties")
public class TetradProperties {

    @Value("${algo.fges.types}")
    private String[] fgesTypes;

    @Value("${algo.gfci.types}")
    private String[] gfciTypes;

    @Value("#{${algo.type.titles}}")
    private Map<String, String> algoTypeTitles;

    @Value("#{${algo.type.desc}}")
    private Map<String, String> algoTypeDescription;

    public TetradProperties() {
    }

    public String[] getFgesTypes() {
        return fgesTypes;
    }

    public void setFgesTypes(String[] fgesTypes) {
        this.fgesTypes = fgesTypes;
    }

    public String[] getGfciTypes() {
        return gfciTypes;
    }

    public void setGfciTypes(String[] gfciTypes) {
        this.gfciTypes = gfciTypes;
    }

    public Map<String, String> getAlgoTypeTitles() {
        return algoTypeTitles;
    }

    public void setAlgoTypeTitles(Map<String, String> algoTypeTitles) {
        this.algoTypeTitles = algoTypeTitles;
    }

    public Map<String, String> getAlgoTypeDescription() {
        return algoTypeDescription;
    }

    public void setAlgoTypeDescription(Map<String, String> algoTypeDescription) {
        this.algoTypeDescription = algoTypeDescription;
    }
    
}
