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

import edu.cmu.tetrad.annotation.AlgorithmAnnotations;
import edu.cmu.tetrad.annotation.Experimental;
import edu.cmu.tetrad.annotation.Nonexecutable;
import edu.pitt.dbmi.causal.web.model.Option;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Mar 13, 2018 3:40:28 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class AlgorithmOpts {

    private static final AlgorithmOpts INSTANCE = new AlgorithmOpts();

    private final Map<String, AlgorithmOpt> algorithMap;
    private final List<Option> options;

    private AlgorithmOpts() {
        AlgorithmAnnotations algoAnno = AlgorithmAnnotations.getInstance();
        List<AlgorithmOpt> list = algoAnno.getAnnotatedClasses().stream()
                .filter(e -> !e.getClazz().isAnnotationPresent(Nonexecutable.class))
                .filter(e -> !e.getClazz().isAnnotationPresent(Experimental.class))
                .map(e -> new AlgorithmOpt(e))
                .sorted()
                .collect(Collectors.toList());

        Map<String, AlgorithmOpt> map = new HashMap<>();
        List<Option> opts = new LinkedList<>();
        list.forEach(e -> {
            String key = e.getAlgorithm().getAnnotation().command();
            map.put(key, e);
            opts.add(new Option(key, e.getAlgorithm().getAnnotation().name()));
        });

        this.algorithMap = Collections.unmodifiableMap(map);
        this.options = Collections.unmodifiableList(opts);
    }

    public static AlgorithmOpts getInstance() {
        return INSTANCE;
    }

    public Map<String, AlgorithmOpt> getAlgorithMap() {
        return algorithMap;
    }

    public List<Option> getOptions() {
        return options;
    }

}
