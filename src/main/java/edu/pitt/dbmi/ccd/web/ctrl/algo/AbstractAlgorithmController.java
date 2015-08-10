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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.FileDelimiter;
import edu.pitt.dbmi.ccd.db.entity.VariableType;
import edu.pitt.dbmi.ccd.db.service.DataFileInfoService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * Aug 6, 2015 10:09:51 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractAlgorithmController {

    protected final String algorithmJar;

    protected final VariableTypeService variableTypeService;

    protected final FileDelimiterService fileDelimiterService;

    protected final DataFileService dataFileService;

    protected final DataFileInfoService dataFileInfoService;

    protected final DataService dataService;

    public AbstractAlgorithmController(
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

    protected String getFileDelimiter(String baseDir, String name) {
        DataFile dataFile = dataFileService.findByAbsolutePathAndName(baseDir, name);
        DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
        FileDelimiter fileDelimiter = dataFileInfo.getFileDelimiter();

        return fileDelimiter.getValue();
    }

    protected Map<String, String> directoryFileListing(String baseDir, String username) {
        Map<String, String> map = new TreeMap<>();

        VariableType variableType = variableTypeService.findByName("continuous");
        List<DataFile> dataFiles = dataService.listDirectorySync(baseDir, username, variableType);
        dataFiles.forEach(file -> {
            String size = FilePrint.humanReadableSize(file.getFileSize(), true);
            String name = file.getName();
            String description = String.format("%s (%s)", name, size);

            map.put(name, description);
        });

        return map;
    }

}
