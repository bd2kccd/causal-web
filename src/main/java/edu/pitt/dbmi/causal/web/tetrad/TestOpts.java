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
import edu.cmu.tetrad.annotation.TestOfIndependence;
import edu.cmu.tetrad.annotation.TestOfIndependenceAnnotations;
import edu.cmu.tetrad.data.DataType;
import edu.cmu.tetrad.util.TetradProperties;
import edu.pitt.dbmi.causal.web.model.Option;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * Mar 14, 2018 5:56:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TestOpts {

    private static final TestOpts INSTANCE = new TestOpts();

    private final Map<DataType, List<Option>> options;
    private final Map<DataType, Option> defaultOptions;
    private final Map<String, TestOpt> testMap;

    private TestOpts() {
        // get annotations
        TestOfIndependenceAnnotations TestAnno = TestOfIndependenceAnnotations.getInstance();
        List<TestOpt> list = TestAnno.getAnnotatedClasses().stream()
                .filter(e -> !e.getClazz().isAnnotationPresent(Experimental.class))
                .map(e -> new TestOpt(e))
                .sorted()
                .collect(Collectors.toList());

        Map<DataType, List<Option>> opts = new EnumMap<>(DataType.class);
        Map<String, TestOpt> tests = new HashMap<>();

        // initialize enum map
        DataType[] dataTypes = DataType.values();
        for (DataType dataType : dataTypes) {
            opts.put(dataType, new LinkedList<>());
        }

        list.forEach(e -> {
            TestOfIndependence anno = e.getTest().getAnnotation();

            // group by datatype
            DataType[] types = anno.dataType();
            for (DataType dataType : types) {
                opts.get(dataType).add(new Option(anno.command(), anno.name()));
            }

            tests.put(anno.command(), e);
        });

        Map<DataType, Option> defaultOpts = new EnumMap<>(DataType.class);
        for (DataType dataType : dataTypes) {
            List<Option> optList = opts.get(dataType);
            if (!optList.isEmpty()) {
                String property = getProperty(dataType);
                if (property == null) {
                    defaultOpts.put(dataType, optList.get(0));
                } else {
                    String value = TetradProperties.getInstance().getValue(property);
                    if (value == null) {
                        defaultOpts.put(dataType, optList.get(0));
                    } else {
                        Optional<Map.Entry<String, TestOpt>> result = tests.entrySet().stream()
                                .filter(e -> e.getValue().getTest().getClazz().getName().equals(value))
                                .findFirst();
                        String name = result.isPresent()
                                ? result.get().getValue().getTest().getAnnotation().command()
                                : null;
                        if (name == null) {
                            defaultOpts.put(dataType, optList.get(0));
                        } else {
                            Optional<Option> resultOpt = optList.stream()
                                    .filter(e -> e.getValue().equals(name))
                                    .findFirst();
                            if (resultOpt.isPresent()) {
                                defaultOpts.put(dataType, resultOpt.get());
                            } else {
                                defaultOpts.put(dataType, optList.get(0));
                            }
                        }
                    }
                }
            }
        }

        this.options = Collections.unmodifiableMap(opts);
        this.defaultOptions = Collections.unmodifiableMap(defaultOpts);
        this.testMap = Collections.unmodifiableMap(tests);
    }

    public static TestOpts getInstance() {
        return INSTANCE;
    }

    public List<Option> getOptions(DataType dataType) {
        return options.get(dataType);
    }

    public TestOpt getTestOpt(String name) {
        return testMap.get(name);
    }

    public Option getDefaultOption(DataType dataType) {
        return defaultOptions.get(dataType);
    }

    private String getProperty(DataType dataType) {
        switch (dataType) {
            case Continuous:
                return "datatype.continuous.test.default";
            case Discrete:
                return "datatype.discrete.test.default";
            case Mixed:
                return "datatype.mixed.test.default";
            default:
                return null;
        }
    }

}
