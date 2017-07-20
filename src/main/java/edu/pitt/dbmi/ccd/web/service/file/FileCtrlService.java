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
package edu.pitt.dbmi.ccd.web.service.file;

import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.web.domain.file.FileSummary;
import edu.pitt.dbmi.ccd.web.domain.file.FileSummaryGroup;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jul 6, 2017 2:33:19 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileCtrlService {

    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final FileTypeService fileTypeService;

    @Autowired
    public FileCtrlService(FileService fileService, FileFormatService fileFormatService, FileTypeService fileTypeService) {
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.fileTypeService = fileTypeService;
    }

    public List<FileSummaryGroup> retrieveFileSummaries(UserAccount userAccount) {
        FileSummaryGroup uncatGroup = new FileSummaryGroup("panel-yellow", new LinkedList<>());
        FileSummaryGroup tetradGroup = new FileSummaryGroup("panel-primary", new LinkedList<>());
        FileSummaryGroup tdiGroup = new FileSummaryGroup("panel-green", new LinkedList<>());

        FileType fileType = fileTypeService.findByName(FileTypeService.RESULT_NAME);
        List<FileFormat> fileFormats = fileFormatService.findByFileTypeNot(fileType);
        fileFormats.forEach(fileFormat -> {
            Long numOfFiles = fileService.getRepository().countByFileFormatAndUserAccount(fileFormat, userAccount);
            switch (fileFormat.getName()) {
                case FileFormatService.TDI_TABULAR_NAME:
                    tdiGroup.getFileSummaries().add(new FileSummary(numOfFiles, fileFormat));
                    break;
                default:
                    tetradGroup.getFileSummaries().add(new FileSummary(numOfFiles, fileFormat));
            }
        });

        Long numOfFiles = fileService.getRepository().countByUserAccountAndFileFormatIsNull(userAccount);
        uncatGroup.getFileSummaries().add(new FileSummary(numOfFiles, null));

        return Arrays.asList(uncatGroup, tetradGroup, tdiGroup);
    }

}
