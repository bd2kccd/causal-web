/*
 * Copyright (C) 2015 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.service.algo;

import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Nov 17, 2015 11:42:41 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgorithmService extends AbstractAlgorithmService {

    @Autowired
    public AlgorithmService(
            @Value("${ccd.server.workspace}") String workspace,
            @Value("${ccd.folder.data:data}") String dataFolder,
            @Value("${ccd.folder.results:results}") String resultFolder,
            @Value("${ccd.folder.lib:lib}") String libFolder,
            @Value("${ccd.folder.tmp:tmp}") String tmpFolder,
            @Value("${ccd.folder.results.algorithm:algorithm}") String algorithmResultFolder,
            DataService dataService,
            DataFileService dataFileService,
            VariableTypeService variableTypeService,
            UserAccountService userAccountService,
            JobQueueInfoService jobQueueInfoService) {
        super(workspace, dataFolder, resultFolder, libFolder, tmpFolder,
                algorithmResultFolder, dataService, dataFileService,
                variableTypeService, userAccountService, jobQueueInfoService);
    }

}
