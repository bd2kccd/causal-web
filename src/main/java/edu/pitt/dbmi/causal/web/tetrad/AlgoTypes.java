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
package edu.pitt.dbmi.causal.web.tetrad;

import edu.cmu.tetrad.annotation.AlgType;
import edu.pitt.dbmi.causal.web.model.Option;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * Mar 20, 2018 4:49:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class AlgoTypes {

    private static final AlgoTypes INSTANCE = new AlgoTypes();

    private final Map<String, AlgType> algoTypeMap;
    private final List<AlgType> algTypes;
    private final List<Option> options;
    public static final String DEFAULT_VALUE = "forbid_latent_common_causes";

    private AlgoTypes() {
        List<AlgType> algoTypes = new LinkedList<>();
        for (AlgType item : AlgType.values()) {
            if (item == AlgType.orient_pairwise) {
                continue;
            }
            algoTypes.add(item);
        }

        Map<String, AlgType> map = new HashMap<>();
        List<Option> opts = new LinkedList<>();
        opts.add(new Option("all", "all"));
        algoTypes.forEach(e -> {
            String value = e.name();
            String text = e.name().replace("_", " ");

            map.put(value, e);
            opts.add(new Option(value, text));
        });

        this.algTypes = Collections.unmodifiableList(algoTypes);
        this.algoTypeMap = Collections.unmodifiableMap(map);
        this.options = Collections.unmodifiableList(opts);
    }

    public static AlgoTypes getInstance() {
        return INSTANCE;
    }

    public AlgType getAlgType(String name) {
        return (name == null) ? null : algoTypeMap.get(name);
    }

    public List<Option> getOptions() {
        return options;
    }

    public List<AlgType> getAlgTypes() {
        return algTypes;
    }

}
