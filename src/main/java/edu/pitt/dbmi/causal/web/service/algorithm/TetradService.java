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
package edu.pitt.dbmi.causal.web.service.algorithm;

import edu.pitt.dbmi.causal.web.model.algorithm.TetradForm;
import edu.pitt.dbmi.causal.web.tetrad.AlgorithmOpts;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.FileGroupService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Mar 5, 2018 2:24:57 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class TetradService {

    private final TetradDataFileService tetradDataFileService;
    private final FileGroupService fileGroupService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public TetradService(TetradDataFileService tetradDataFileService, FileGroupService fileGroupService, VariableTypeService variableTypeService) {
        this.tetradDataFileService = tetradDataFileService;
        this.fileGroupService = fileGroupService;
        this.variableTypeService = variableTypeService;
    }

    public TetradForm createTetradForm() {
        TetradForm tetradForm = new TetradForm();

        List<VariableType> varTypes = variableTypeService.findAll();
        if (!varTypes.isEmpty()) {
            tetradForm.setVarTypeId(varTypes.get(0).getId());
        }

        String algorithm = AlgorithmOpts.getInstance()
                .getAlgorithMap().keySet().stream().findFirst().get();
        tetradForm.setAlgorithm(algorithm);

        return tetradForm;
    }

}
