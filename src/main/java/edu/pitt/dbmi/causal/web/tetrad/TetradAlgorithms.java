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
import edu.cmu.tetrad.annotation.Algorithm;
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

    private final Map<String, TetradAlgorithm> algoByCmdName;
    private final Map<AlgType, List<Option>> optionsByAlgType;
    private final Map<AlgType, List<Option>> multiDatasetOptionsByAlgType;
    private final Map<AlgType, List<Option>> knowledgeOptionsByAlgType;
    private final Map<AlgType, List<Option>> multiDatasetOptionsAndknowledgeOptionsByAlgType;
    private final List<Option> options;
    private final List<Option> multiDatasetOptions;
    private final List<Option> knowledgeOptions;
    private final List<Option> multiDatasetOptionsAndknowledgeOptions;

    private TetradAlgorithms() {
        AlgorithmAnnotations algoAnno = AlgorithmAnnotations.getInstance();
        List<TetradAlgorithm> list = algoAnno.getAnnotatedClasses().stream()
                .filter(e -> e.getAnnotation().algoType() != AlgType.orient_pairwise)
                .filter(e -> !e.getClazz().isAnnotationPresent(Nonexecutable.class))
                .filter(e -> !e.getClazz().isAnnotationPresent(Experimental.class))
                .map(e -> new TetradAlgorithm(e))
                .sorted()
                .collect(Collectors.toList());

        this.algoByCmdName = list.stream()
                .collect(Collectors.toMap(e -> e.getAlgorithm().getAnnotation().command(), Function.identity()));

        Map<AlgType, List<Option>> allOptsByAlgType = new EnumMap<>(AlgType.class);
        Map<AlgType, List<Option>> multiDataOptsByAlgType = new EnumMap<>(AlgType.class);
        Map<AlgType, List<Option>> knwlOptsByAlgType = new EnumMap<>(AlgType.class);
        Map<AlgType, List<Option>> multiDataAndKnwlOptsByAlgType = new EnumMap<>(AlgType.class);
        for (AlgType algType : AlgType.values()) {
            allOptsByAlgType.put(algType, new LinkedList<>());
            multiDataOptsByAlgType.put(algType, new LinkedList<>());
            knwlOptsByAlgType.put(algType, new LinkedList<>());
            multiDataAndKnwlOptsByAlgType.put(algType, new LinkedList<>());
        }

        List<Option> allOpts = new LinkedList<>();
        List<Option> multiDataOpts = new LinkedList<>();
        List<Option> knwlOpts = new LinkedList<>();
        List<Option> multiDataAndKnwlOpts = new LinkedList<>();

        list.forEach(e -> {
            boolean acceptKnowledge = e.isAcceptKnowledge();
            boolean acceptMultiDataset = e.isAcceptMultiDataset();

            Algorithm algorithm = e.getAlgorithm().getAnnotation();
            AlgType algType = algorithm.algoType();
            String value = algorithm.command();
            String text = algorithm.name();

            Option opt = new Option(value, text);

            allOpts.add(opt);
            allOptsByAlgType.get(algType).add(opt);
            if (acceptKnowledge && acceptMultiDataset) {
                multiDataAndKnwlOpts.add(opt);
                multiDataAndKnwlOptsByAlgType.get(algType).add(opt);
            }
            if (acceptKnowledge) {
                knwlOpts.add(opt);
                knwlOptsByAlgType.get(algType).add(opt);
            }
            if (acceptMultiDataset) {
                multiDataOpts.add(opt);
                multiDataOptsByAlgType.get(algType).add(opt);
            }
        });

        this.optionsByAlgType = new EnumMap<>(AlgType.class);
        allOptsByAlgType.forEach((k, v) -> optionsByAlgType.put(k, Collections.unmodifiableList(v)));

        this.multiDatasetOptionsByAlgType = new EnumMap<>(AlgType.class);
        multiDataOptsByAlgType.forEach((k, v) -> multiDatasetOptionsByAlgType.put(k, Collections.unmodifiableList(v)));

        this.knowledgeOptionsByAlgType = new EnumMap<>(AlgType.class);
        knwlOptsByAlgType.forEach((k, v) -> knowledgeOptionsByAlgType.put(k, Collections.unmodifiableList(v)));

        this.multiDatasetOptionsAndknowledgeOptionsByAlgType = new EnumMap<>(AlgType.class);
        multiDataAndKnwlOptsByAlgType.forEach((k, v) -> multiDatasetOptionsAndknowledgeOptionsByAlgType.put(k, Collections.unmodifiableList(v)));

        this.options = Collections.unmodifiableList(allOpts);
        this.multiDatasetOptions = Collections.unmodifiableList(multiDataOpts);
        this.knowledgeOptions = Collections.unmodifiableList(knwlOpts);
        this.multiDatasetOptionsAndknowledgeOptions = Collections.unmodifiableList(multiDataAndKnwlOpts);
    }

    public TetradAlgorithm getTetradAlgorithm(String shortName) {
        return (shortName == null) ? null : algoByCmdName.get(shortName);
    }

    public List<Option> getOptions(AlgType algType) {
        if (algType == null || !optionsByAlgType.containsKey(algType)) {
            return Collections.EMPTY_LIST;
        }

        return optionsByAlgType.get(algType);
    }

    public List<Option> getMultiDatasetOptions(AlgType algType) {
        if (algType == null || !multiDatasetOptionsByAlgType.containsKey(algType)) {
            return Collections.EMPTY_LIST;
        }

        return multiDatasetOptionsByAlgType.get(algType);
    }

    public List<Option> getKnowledgeOptions(AlgType algType) {
        if (algType == null || !knowledgeOptionsByAlgType.containsKey(algType)) {
            return Collections.EMPTY_LIST;
        }

        return knowledgeOptionsByAlgType.get(algType);
    }

    public List<Option> getMultiDatasetOptionsAndknowledgeOptions(AlgType algType) {
        if (algType == null || !multiDatasetOptionsAndknowledgeOptionsByAlgType.containsKey(algType)) {
            return Collections.EMPTY_LIST;
        }

        return multiDatasetOptionsAndknowledgeOptionsByAlgType.get(algType);
    }

    public List<Option> getOptions() {
        return options;
    }

    public List<Option> getMultiDatasetOptions() {
        return multiDatasetOptions;
    }

    public List<Option> getKnowledgeOptions() {
        return knowledgeOptions;
    }

    public List<Option> getMultiDatasetOptionsAndknowledgeOptions() {
        return multiDatasetOptionsAndknowledgeOptions;
    }

    public static TetradAlgorithms getInstance() {
        return INSTANCE;
    }

}
