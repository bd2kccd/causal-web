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
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileInfoService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.model.DataListItem;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * Apr 7, 2015 9:52:57 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AlgorithmController {

    protected final String algorithmJar;

    protected final VariableTypeService variableTypeService;

    protected final FileDelimiterService fileDelimiterService;

    protected final DataFileService dataFileService;

    protected final DataFileInfoService dataFileInfoService;

    protected final DataService dataService;

    public AlgorithmController(
            String algorithmJar,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            DataFileService dataFileService,
            DataFileInfoService dataFileInfoService,
            DataService dataService) {
        this.algorithmJar = algorithmJar;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.dataFileService = dataFileService;
        this.dataFileInfoService = dataFileInfoService;
        this.dataService = dataService;
    }

    protected String getFileDelimiter(String name, String absolutePath) {
        return "\t";
//        DataFileInfo dataFileInfo = dataFileInfoService.getDataFileInfoRepository()
//                .findByDataFileNameAndAbsolutePath(name, absolutePath);
//
//        return FileInfos.delimiterNameToString(dataFileInfo.getFileDelimiter().getName());
    }

    protected Map<String, String> directoryFileListing(String baseDir) {
        Map<String, String> map = new TreeMap<>();

        VariableType variableType = variableTypeService.findByName("continuous");
        List<DataListItem> dataListItems = dataService.createListItem(baseDir, variableType);
        dataListItems.forEach(item -> {
            String key = item.getFileName();
            String value = String.format(("%s (%s)"), item.getFileName(), item.getSize());
            map.put(key, value);
        });

        return map;
    }

}
