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
package edu.pitt.dbmi.ccd.web.service.algo;

import edu.pitt.dbmi.ccd.web.domain.Algorithm;
import edu.pitt.dbmi.ccd.web.prop.TetradProperties;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 14, 2017 4:48:03 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class CausalDiscoveryCtrlService {

    private final TetradProperties tetradProperties;

    @Autowired
    public CausalDiscoveryCtrlService(TetradProperties tetradProperties) {
        this.tetradProperties = tetradProperties;
    }

    public List<Algorithm> retrieveAlgorithms() {
        List<Algorithm> algorithms = new LinkedList<>();

        String[] algorithNames = tetradProperties.getAlgorithNames();
        Map<String, String> algorithmTitles = tetradProperties.getAlgorithmTitles();
        Map<String, String> algorithmDescriptions = tetradProperties.getAlgorithmDescriptions();

        for (String algorithmName : algorithNames) {
            algorithms.add(new Algorithm(algorithmName, algorithmTitles.get(algorithmName), algorithmDescriptions.get(algorithmName)));
        }

        return algorithms;
    }

}
