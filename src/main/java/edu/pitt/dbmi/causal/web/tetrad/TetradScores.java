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

import edu.cmu.tetrad.annotation.Experimental;
import edu.cmu.tetrad.annotation.Score;
import edu.cmu.tetrad.annotation.ScoreAnnotations;
import edu.cmu.tetrad.data.DataType;
import edu.cmu.tetrad.util.TetradProperties;
import edu.pitt.dbmi.causal.web.model.Option;
import edu.pitt.dbmi.causal.web.model.OptionModel;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * Mar 14, 2018 3:27:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradScores {

    private static final TetradScores INSTANCE = new TetradScores();

    private final Map<String, TetradScore> scoreByShortName;
    private final Map<DataType, List<Option>> optsByDataType;
    private final Map<DataType, Option> defaultOpts;
    private final Map<DataType, OptionModel> optModels;

    private TetradScores() {
        ScoreAnnotations scoreAnno = ScoreAnnotations.getInstance();
        List<TetradScore> list = scoreAnno.getAnnotatedClasses().stream()
                .filter(e -> !e.getClazz().isAnnotationPresent(Experimental.class))
                .map(e -> new TetradScore(e))
                .sorted()
                .collect(Collectors.toList());

        this.scoreByShortName = list.stream()
                .collect(Collectors.toMap(e -> e.getScore().getAnnotation().command(), Function.identity()));

        this.optsByDataType = new EnumMap<>(DataType.class);
        Arrays.stream(DataType.values())
                .forEach(dataType -> optsByDataType.put(dataType, new LinkedList<>()));
        list.forEach(e -> {
            Score anno = e.getScore().getAnnotation();

            // group by datatype
            Arrays.stream(anno.dataType())
                    .forEach(dataType -> optsByDataType
                    .get(dataType)
                    .add(new Option(anno.command(), anno.name())));
        });

        defaultOpts = new EnumMap<>(DataType.class);
        Arrays.stream(DataType.values()).forEach(dataType -> {
            List<Option> optList = optsByDataType.get(dataType);
            if (!optList.isEmpty()) {
                String prop = getProperty(dataType);
                if (prop == null) {
                    defaultOpts.put(dataType, optList.get(0));
                } else {
                    String value = TetradProperties.getInstance().getValue(prop);
                    if (value == null) {
                        defaultOpts.put(dataType, optList.get(0));
                    } else {
                        Optional<Map.Entry<String, TetradScore>> score = scoreByShortName.entrySet().stream()
                                .filter(e -> e.getValue().getScore().getClazz().getName().equals(value))
                                .findFirst();
                        if (score.isPresent()) {
                            String shortName = score.get().getValue().getScore().getAnnotation().command();
                            Optional<Option> opt = optList.stream()
                                    .filter(e -> e.getValue().equals(shortName))
                                    .findFirst();
                            if (opt.isPresent()) {
                                defaultOpts.put(dataType, opt.get());
                            } else {
                                defaultOpts.put(dataType, optList.get(0));
                            }
                        } else {
                            defaultOpts.put(dataType, optList.get(0));
                        }
                    }
                }
            }
        });

        this.optModels = Arrays.stream(DataType.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> new OptionModel(optsByDataType.get(e), defaultOpts.get(e).getValue())
                ));
    }

    public static TetradScores getInstance() {
        return INSTANCE;
    }

    private String getProperty(DataType dataType) {
        switch (dataType) {
            case Continuous:
                return "datatype.continuous.score.default";
            case Discrete:
                return "datatype.discrete.score.default";
            case Mixed:
                return "datatype.mixed.score.default";
            default:
                return null;
        }
    }

    public TetradScore getTetradScore(String shortName) {
        return scoreByShortName.get(shortName);
    }

    public List<Option> getOptions(DataType dataType) {
        return optsByDataType.get(dataType);
    }

    public Option getDefaultOption(DataType dataType) {
        return defaultOpts.get(dataType);
    }

    public OptionModel getOptionModel(DataType dataType) {
        return optModels.get(dataType);
    }

}
