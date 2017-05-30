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
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.web.model.algo.TetradAlgoOpt;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * May 28, 2017 11:28:37 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractTetradAlgoController {

    protected List<String> getDataset(TetradAlgoOpt tetradAlgoOpt) {
        return Collections.singletonList(tetradAlgoOpt.getDataset());
    }

    protected List<String> getPriorKnowledge(TetradAlgoOpt tetradAlgoOpt) {
        String priorKnowledge = tetradAlgoOpt.getPriorKnowledge();
        if (priorKnowledge.trim().length() == 0) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.singletonList(tetradAlgoOpt.getPriorKnowledge());
        }
    }

    protected List<String> getJvmOptions(TetradAlgoOpt tetradAlgoOpt) {
        List<String> jvmOptions = new LinkedList<>();

        int jvmMaxMem = tetradAlgoOpt.getJvmMaxMem();
        if (jvmMaxMem > 0) {
            jvmOptions.add(String.format("-Xmx%dG", jvmMaxMem));
        }

        return jvmOptions;
    }

}
