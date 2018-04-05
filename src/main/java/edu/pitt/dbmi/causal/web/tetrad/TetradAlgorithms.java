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
import edu.cmu.tetrad.annotation.AlgorithmAnnotations;
import edu.cmu.tetrad.annotation.Experimental;
import edu.cmu.tetrad.annotation.Nonexecutable;
import edu.pitt.dbmi.causal.web.model.Option;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * Mar 13, 2018 3:40:28 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradAlgorithms {

    private static final TetradAlgorithms INSTANCE = new TetradAlgorithms();

    private final Map<String, TetradAlgorithm> algoByShortName;
    private final Map<AlgType, List<Option>> optionsByAlgType;
    private final List<Option> options;

    private TetradAlgorithms() {
        AlgorithmAnnotations algoAnno = AlgorithmAnnotations.getInstance();
        List<TetradAlgorithm> list = algoAnno.getAnnotatedClasses().stream()
                .filter(e -> e.getAnnotation().algoType() != AlgType.orient_pairwise)
                .filter(e -> !e.getClazz().isAnnotationPresent(Nonexecutable.class))
                .filter(e -> !e.getClazz().isAnnotationPresent(Experimental.class))
                .map(e -> new TetradAlgorithm(e))
                .sorted()
                .collect(Collectors.toList());

        this.algoByShortName = list.stream()
                .collect(Collectors.toMap(e -> e.getAlgorithm().getAnnotation().command(), Function.identity()));

        this.optionsByAlgType = new EnumMap<>(AlgType.class);
        List<Option> opts = new LinkedList<>();
        list.forEach(e -> {
            AlgType algType = e.getAlgorithm().getAnnotation().algoType();
            String value = e.getAlgorithm().getAnnotation().command();
            String text = e.getAlgorithm().getAnnotation().name();

            Option opt = new Option(value, text);

            List<Option> listOpt = optionsByAlgType.get(algType);
            if (listOpt == null) {
                listOpt = new LinkedList<>();
                optionsByAlgType.put(algType, listOpt);
            }

            listOpt.add(opt);
            opts.add(opt);
        });

        this.options = Collections.unmodifiableList(opts);
    }

    public TetradAlgorithm getTetradAlgorithm(String shortName) {
        return (shortName == null)
                ? null
                : algoByShortName.get(shortName);
    }

    public List<Option> getOptions() {
        return options;
    }

    public List<Option> getOptions(AlgType algType) {
        return (algType == null)
                ? null
                : optionsByAlgType.get(algType);
    }

    public static TetradAlgorithms getInstance() {
        return INSTANCE;
    }

}
