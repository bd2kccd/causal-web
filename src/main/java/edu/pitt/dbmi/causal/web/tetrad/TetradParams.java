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

import edu.cmu.tetrad.util.ParamDescription;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.pitt.dbmi.causal.web.model.ParamOption;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Mar 26, 2018 2:13:08 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class TetradParams {

    private static final TetradParams INSTANCE = new TetradParams();

    private final Map<String, ParamOption> params = new HashMap<>();

    private TetradParams() {
        ParamDescriptions paramDescs = ParamDescriptions.getInstance();
        paramDescs.getNames().forEach(e -> {
            ParamDescription paramDesc = paramDescs.get(e);
            Class type = paramDesc.getDefaultValue().getClass();
            boolean isInteger = (type == Integer.class);
            boolean isDouble = (type == Double.class);
            boolean isBoolean = (type == Boolean.class);

            ParamOption opt = new ParamOption(e, paramDesc.getDescription());
            opt.setDefaultVal(String.valueOf(paramDesc.getDefaultValue()));
            opt.setaBoolean(isBoolean);
            if (isInteger || isDouble) {
                opt.setNumeric(true);

                if (type == Integer.class) {
                    opt.setMinVal(Integer.toString(paramDesc.getLowerBoundInt()));
                    opt.setMaxVal(Integer.toString(paramDesc.getUpperBoundInt()));
                } else {
                    opt.setMinVal(Double.toString(paramDesc.getLowerBoundDouble()));
                    opt.setMaxVal(Double.toString(paramDesc.getUpperBoundDouble()));
                }
            }

            params.put(e, opt);
        });
    }

    public ParamOption getParamOption(String shortName) {
        return params.get(shortName);
    }

    public static TetradParams getInstance() {
        return INSTANCE;
    }

}
