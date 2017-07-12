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
import edu.pitt.dbmi.ccd.db.repository.FileRepository;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.web.domain.file.FileListView;
import edu.pitt.dbmi.ccd.web.domain.file.FileSummary;
import edu.pitt.dbmi.ccd.web.domain.file.FileSummaryGroup;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
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
    private final FileManagementService fileManagementService;

    @Autowired
    public FileCtrlService(FileService fileService, FileFormatService fileFormatService, FileTypeService fileTypeService, FileManagementService fileManagementService) {
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.fileTypeService = fileTypeService;
        this.fileManagementService = fileManagementService;
    }

    public FileListView getFileListView(String fileFormatName) {
        switch (fileFormatName) {
            case FileFormatService.TETRAD_TABULAR:
                return new FileListView("CCD: Tetrad Tabular Data", "Tetrad Tabular Data Files", fileFormatName);
            case FileFormatService.TETRAD_COVARIANCE:
                return new FileListView("CCD: Tetrad Covariance", "Tetrad Covariance Files", fileFormatName);
            case FileFormatService.TETRAD_VARIABLE:
                return new FileListView("CCD: Tetrad Variable", "Tetrad Variable Files", fileFormatName);
            case FileFormatService.TETRAD_KNOWLEDGE:
                return new FileListView("CCD: Tetrad Knowledge", "Tetrad Knowledge Files", fileFormatName);
            case FileFormatService.TDI_TABULAR:
                return new FileListView("CCD: TDI Tabular Data", "TDI Tabular Data Files", fileFormatName);
            default:
                return new FileListView("CCD: Uncategorize File", "Uncategorized Files", fileFormatName);
        }
    }

    public List<FileSummaryGroup> getFileSummaryGroups(UserAccount userAccount) {
        List<FileSummaryGroup> groups = new LinkedList<>();

        FileSummaryGroup uncatGroup = new FileSummaryGroup("panel-yellow", new LinkedList<>());
        FileSummaryGroup tetradGroup = new FileSummaryGroup("panel-primary", new LinkedList<>());
        FileSummaryGroup tdiGroup = new FileSummaryGroup("panel-green", new LinkedList<>());

        FileRepository fileRepository = fileService.getRepository();

        FileType excludeFileType = fileTypeService.findByName(FileTypeService.RESULT);
        List<FileFormat> fileFormats = fileFormatService.findByFileTypeNot(excludeFileType);
        fileFormats.forEach(fileFormat -> {
            String title = fileFormat.getDisplayName();
            String fileFormatName = fileFormat.getName();
            Long count = fileRepository.countByFileFormatAndUserAccount(fileFormat, userAccount);

            FileSummary fileSummary = new FileSummary(title, fileFormatName, count);
            switch (fileFormatName) {
                case FileFormatService.TDI_TABULAR:
                    tdiGroup.getFileSummaries().add(fileSummary);
                    break;
                default:
                    tetradGroup.getFileSummaries().add(fileSummary);
            }
        });

        String title = "Uncategorized";
        String fileFormatName = "uncategorized";
        Long count = fileRepository.countByUserAccountAndFileFormatIsNull(userAccount);
        uncatGroup.getFileSummaries().add(new FileSummary(title, fileFormatName, count));

        return Arrays.asList(uncatGroup, tetradGroup, tdiGroup);
    }

}
