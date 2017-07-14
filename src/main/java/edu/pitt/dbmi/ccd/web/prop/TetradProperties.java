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

    @Value("${tetrad.algorithm.name:}")
    private String[] algorithNames;

    @Value("#{${tetrad.algorithm.title:}}")
    private Map<String, String> algorithmTitles;

    @Value("#{${tetrad.algorithm.description:}}")
    private Map<String, String> algorithmDescriptions;

    public TetradProperties() {
    }

    public String[] getAlgorithNames() {
        return algorithNames;
    }

    public void setAlgorithNames(String[] algorithNames) {
        this.algorithNames = algorithNames;
    }

    public Map<String, String> getAlgorithmTitles() {
        return algorithmTitles;
    }

    public void setAlgorithmTitles(Map<String, String> algorithmTitles) {
        this.algorithmTitles = algorithmTitles;
    }

    public Map<String, String> getAlgorithmDescriptions() {
        return algorithmDescriptions;
    }

    public void setAlgorithmDescriptions(Map<String, String> algorithmDescriptions) {
        this.algorithmDescriptions = algorithmDescriptions;
    }

}
