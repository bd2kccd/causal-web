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
package edu.pitt.dbmi.ccd.web.ctrl.algorithm;

import edu.pitt.dbmi.ccd.web.domain.algorithm.AlgorithmItem;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 13, 2017 5:05:39 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/causal-discover")
public class CausalDiscoveryController implements ViewPath {

    private final Map<String, List<AlgorithmItem>> algorithms;

    @Autowired
    public CausalDiscoveryController(Map<String, List<AlgorithmItem>> algorithms) {
        this.algorithms = algorithms;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showCausalDiscoveryAlgorithms(final Model model) {
        model.addAttribute("algorithms", algorithms);

        return CAUSAL_DISCOVER_VIEW;
    }

}
