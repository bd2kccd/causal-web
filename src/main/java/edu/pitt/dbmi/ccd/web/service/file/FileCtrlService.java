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

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.repository.FileRepository;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.web.domain.file.FileCategory;
import edu.pitt.dbmi.ccd.web.domain.file.FileCategoryPanel;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
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
    private final FileManagementService fileManagementService;

    @Autowired
    public FileCtrlService(FileService fileService, FileManagementService fileManagementService) {
        this.fileService = fileService;
        this.fileManagementService = fileManagementService;
    }

    public List<FileCategoryPanel> getFileCategoryPanels(UserAccount userAccount) {
        List<FileCategoryPanel> fileCategoryPanels = new LinkedList<>();

        FileRepository fileRepository = fileService.getRepository();
        long uncatCount = fileRepository.countByUserAccountAndFileFormatIsNull(userAccount);
        long tetradTabCount = fileRepository.countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_TABULAR, userAccount);
        long tetradCovCount = fileRepository.countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_COVARIANCE, userAccount);
        long tetradVarCount = fileRepository.countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_VARIABLE, userAccount);
        long tetradKnowCount = fileRepository.countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_KNOWLEDGE, userAccount);
        long tdiTabCount = fileRepository.countByFileFormatNameAndUserAccount(FileFormatService.TDI_TABULAR, userAccount);

        List<FileCategory> fileCategories = new LinkedList<>();
        fileCategories.add(new FileCategory("Uncategorized", uncatCount, "uncategorized"));
        fileCategoryPanels.add(new FileCategoryPanel("Uncategorized Files", "Uploaded files that have not been categorized.", "panel-yellow", fileCategories));

        fileCategories = new LinkedList<>();
        fileCategories.add(new FileCategory("Tabular Data", tetradTabCount, FileFormatService.TETRAD_TABULAR));
        fileCategories.add(new FileCategory("Covariance", tetradCovCount, FileFormatService.TETRAD_COVARIANCE));
        fileCategories.add(new FileCategory("Variable", tetradVarCount, FileFormatService.TETRAD_VARIABLE));
        fileCategories.add(new FileCategory("Knowledge", tetradKnowCount, FileFormatService.TETRAD_KNOWLEDGE));
        fileCategoryPanels.add(new FileCategoryPanel("Tetrad Files", "Files that are associated with Tetrad algorithms.", "panel-primary", fileCategories));

        fileCategories = new LinkedList<>();
        fileCategories.add(new FileCategory("Tabular Data", tdiTabCount, FileFormatService.TDI_TABULAR));
        fileCategoryPanels.add(new FileCategoryPanel("TDI Files", "Files that are associated with TDI algorithms.", "panel-green", fileCategories));

        return fileCategoryPanels;
    }

}
