/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.dbmi.ccd.web.ctrl.file;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.UNCATEGORIZED_FILE_VIEW;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.fs.FileManagementService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jun 11, 2017 5:56:42 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/tetrad/data")
public class TetradDataFileController {

    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final FileService fileService;

    @Autowired
    public TetradDataFileController(
            AppUserService appUserService,
            FileManagementService fileManagementService,
            FileService fileService) {
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFiles(final AppUser appUser, final Model model) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        fileManagementService.syncDatabaseWithDataDirectory(userAccount);

        return UNCATEGORIZED_FILE_VIEW;
    }

    @ResponseBody
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listFiles(final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            return ResponseEntity.notFound().build();
        }

        List<File> file = fileService.getRepository().findByUserAccountAndFileFormatIsNull(userAccount);

        return ResponseEntity.ok(file);
    }

}
