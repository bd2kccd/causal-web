/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.prop;

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

    @Value("${tetrad.algo.fges}")
    private String[] fgesAlgos;

    @Value("${tetrad.algo.gfci}")
    private String[] gfciAlgos;

    @Value("#{${tetrad.algo.titles}}")
    private Map<String, String> algoTitles;

    @Value("#{${tetrad.algo.descriptions}}")
    private Map<String, String> algoDescriptions;

    @Value("#{${tetrad.algo.param.labels}}")
    private Map<String, String> algoParamLabels;

    public TetradProperties() {
    }

    public String[] getFgesAlgos() {
        return fgesAlgos;
    }

    public void setFgesAlgos(String[] fgesAlgos) {
        this.fgesAlgos = fgesAlgos;
    }

    public String[] getGfciAlgos() {
        return gfciAlgos;
    }

    public void setGfciAlgos(String[] gfciAlgos) {
        this.gfciAlgos = gfciAlgos;
    }

    public Map<String, String> getAlgoTitles() {
        return algoTitles;
    }

    public void setAlgoTitles(Map<String, String> algoTitles) {
        this.algoTitles = algoTitles;
    }

    public Map<String, String> getAlgoDescriptions() {
        return algoDescriptions;
    }

    public void setAlgoDescriptions(Map<String, String> algoDescriptions) {
        this.algoDescriptions = algoDescriptions;
    }

    public Map<String, String> getAlgoParamLabels() {
        return algoParamLabels;
    }

    public void setAlgoParamLabels(Map<String, String> algoParamLabels) {
        this.algoParamLabels = algoParamLabels;
    }

}
