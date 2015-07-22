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

import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.web.model.DataListItem;
import edu.pitt.dbmi.ccd.web.service.DataFileService;
import edu.pitt.dbmi.ccd.web.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.web.service.VariableTypeService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public AlgorithmController(String algorithmJar, VariableTypeService variableTypeService, FileDelimiterService fileDelimiterService, DataFileService dataFileService) {
        this.algorithmJar = algorithmJar;
        this.variableTypeService = variableTypeService;
        this.fileDelimiterService = fileDelimiterService;
        this.dataFileService = dataFileService;
    }

    protected Map<String, String> directoryFileListing(Path directory) {
        Map<String, String> map = new TreeMap<>();

        try {
            List<Path> list = FileInfos.listDirectory(directory, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            VariableType variableType = variableTypeService.getVariableTypeRepository().findByName("continuous");
            List<DataListItem> dataListItems = dataFileService.generateListItem(files, variableType);
            dataListItems.forEach(item -> {
                String key = item.getFileName();
                String value = String.format(("%s (%s)"), item.getFileName(), item.getSize());
                map.put(key, value);
            });
        } catch (IOException exception) {
        }
        return map;
    }

}
