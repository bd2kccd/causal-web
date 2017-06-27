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
package edu.pitt.dbmi.ccd.web.ctrl.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.FILE_LIST_VIEW;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jun 26, 2017 5:14:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file")
public class FileController implements ViewPath {

    private final FileService fileService;
    private final FileManagementService fileManagementService;
    private final AppUserService appUserService;

    @Autowired
    public FileController(FileService fileService, FileManagementService fileManagementService, AppUserService appUserService) {
        this.fileService = fileService;
        this.fileManagementService = fileManagementService;
        this.appUserService = appUserService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String categorizeFile(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        long uncatCount = fileService.getRepository().countByUserAccountAndFileFormatIsNull(userAccount);
        long tetradTabCount = fileService.getRepository().countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_TABULAR, userAccount);
        long tetradCovCount = fileService.getRepository().countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_COVARIANCE, userAccount);
        long tetradVarCount = fileService.getRepository().countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_VARIABLE, userAccount);
        long tetradKnowCount = fileService.getRepository().countByFileFormatNameAndUserAccount(FileFormatService.TETRAD_KNOWLEDGE, userAccount);
        long tdiTabCount = fileService.getRepository().countByFileFormatNameAndUserAccount(FileFormatService.TDI_TABULAR, userAccount);

        model.addAttribute("uncatCount", uncatCount);
        model.addAttribute("tetradTabCount", tetradTabCount);
        model.addAttribute("tetradCovCount", tetradCovCount);
        model.addAttribute("tetradVarCount", tetradVarCount);
        model.addAttribute("tetradKnowCount", tetradKnowCount);
        model.addAttribute("tdiTabCount", tdiTabCount);

        return FILE_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list/{fileType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFiles(final @PathVariable String fileType, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        List<File> files = new LinkedList<>();
        switch (fileType) {
            case FileFormatService.TETRAD_TABULAR:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_TABULAR, userAccount));
                break;
            case FileFormatService.TETRAD_COVARIANCE:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_COVARIANCE, userAccount));
                break;
            case FileFormatService.TETRAD_VARIABLE:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_VARIABLE, userAccount));
                break;
            case FileFormatService.TETRAD_KNOWLEDGE:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TETRAD_KNOWLEDGE, userAccount));
                break;
            case FileFormatService.TDI_TABULAR:
                files.addAll(fileService.findByUserAccountAndFileFormatName(FileFormatService.TDI_TABULAR, userAccount));
                break;
            case "uncategorized":
                files.addAll(fileService.findByUserAccountAndFileFormatName(null, userAccount));
                break;
            default:
                return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(files);
    }

    @RequestMapping(value = "{fileType}", method = RequestMethod.GET)
    public String showFiles(@PathVariable String fileType, final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        fileManagementService.syncDatabaseWithDataDirectory(userAccount);

        String pageTitle;
        String title;
        switch (fileType) {
            case FileFormatService.TETRAD_TABULAR:
                pageTitle = "CCD: Tetrad Tabular Data";
                title = "Tetrad Tabular Data Files";
                break;
            case FileFormatService.TETRAD_COVARIANCE:
                pageTitle = "CCD: Tetrad Covariance";
                title = "Tetrad Covariance Files";
                break;
            case FileFormatService.TETRAD_VARIABLE:
                pageTitle = "CCD: Tetrad Variable";
                title = "Tetrad Variable Files";
                break;
            case FileFormatService.TETRAD_KNOWLEDGE:
                pageTitle = "CCD: Tetrad Knowledge";
                title = "Tetrad Knowledge Files";
                break;
            case FileFormatService.TDI_TABULAR:
                pageTitle = "CCD: TDI Tabular Data";
                title = "TDI Tabular Data Files";
                break;
            case "uncategorized":
                pageTitle = "CCD: Uncategorize File";
                title = "Uncategorized Files";
                break;
            default:
                throw new ResourceNotFoundException();
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("title", title);
        model.addAttribute("fileType", fileType);

        return FILE_LIST_VIEW;
    }

}
