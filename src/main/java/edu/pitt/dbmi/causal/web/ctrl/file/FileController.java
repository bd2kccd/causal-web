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
package edu.pitt.dbmi.causal.web.ctrl.file;

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.file.FileCategorizeForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.FileCategorizeService;
import edu.pitt.dbmi.causal.web.service.filesys.FileManagementService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.AlgorithmTypeService;
import edu.pitt.dbmi.ccd.db.service.DataDelimiterService;
import edu.pitt.dbmi.ccd.db.service.FileFormatService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 26, 2017 5:14:52 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file")
public class FileController {

    private final AppUserService appUserService;
    private final FileManagementService fileManagementService;
    private final FileService fileService;
    private final FileFormatService fileFormatService;
    private final FileCategorizeService fileCategorizeService;
    private final DataDelimiterService dataDelimiterService;
    private final VariableTypeService variableTypeService;

    @Autowired
    public FileController(
            AppUserService appUserService,
            FileManagementService fileManagementService,
            FileService fileService,
            AlgorithmTypeService algorithmTypeService,
            FileFormatService fileFormatService,
            FileCategorizeService fileCategorizeService,
            DataDelimiterService dataDelimiterService,
            VariableTypeService variableTypeService) {
        this.appUserService = appUserService;
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
        this.fileFormatService = fileFormatService;
        this.fileCategorizeService = fileCategorizeService;
        this.dataDelimiterService = dataDelimiterService;
        this.variableTypeService = variableTypeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "{fileId}", method = RequestMethod.POST)
    public String categorizeFile(
            @Valid @ModelAttribute("fileCategorizeForm") final FileCategorizeForm fileCategorizeForm,
            final BindingResult bindingResult,
            @PathVariable final Long fileId,
            final Model model,
            final AppUser appUser,
            final RedirectAttributes redirAttrs) {
        if (bindingResult.hasErrors()) {
            redirAttrs.addFlashAttribute("org.springframework.validation.BindingResult.fileCategorizeForm", bindingResult);
            redirAttrs.addFlashAttribute("fileCategorizeForm", fileCategorizeForm);

            return ViewPath.REDIRECT_FILE_INFO + fileId;
        }

        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(fileId, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        FileFormat fileFmt = fileCategorizeService.categorizeFile(fileCategorizeForm, file, userAccount);

        return (fileFmt == null)
                ? ViewPath.REDIRECT_UNCATEGORIZED_FILE_VIEW
                : ViewPath.REDIRECT_FILE_LIST + fileFmt.getId();
    }

    @RequestMapping(value = "{fileId}", method = RequestMethod.GET)
    public String showFileInfoAndCategory(@PathVariable final Long fileId, final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        File file = fileService.getRepository().findByIdAndUserAccount(fileId, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("file", file);
        model.addAttribute("moreInfo", fileService.getAdditionalInfo(file));
        model.addAttribute("fileCategorizeForm", fileCategorizeService.createForm(file));
        model.addAttribute("formatOpts", fileFormatService.getFileFormatOptions());
        model.addAttribute("delimOpts", dataDelimiterService.findAll());
        model.addAttribute("varOpts", variableTypeService.findAll());
        model.addAttribute("tetradTabFileFormatId", fileFormatService.findByShortName(FileFormatService.TETRAD_TAB_SHORT_NAME).getId());

        return ViewPath.FILE_INFO_VIEW;
    }

    @RequestMapping(value = "format/{id}", method = RequestMethod.GET)
    public String showFileList(@PathVariable final Long id, final Model model, final AppUser appUser) {
        Optional<FileFormat> fileFormat = fileFormatService.findById(id);
        if (fileFormat.isPresent()) {
            model.addAttribute("fileFormat", fileFormat.get());
        } else {
            throw new ResourceNotFoundException();
        }

        return ViewPath.FILE_LIST_VIEW;
    }

    @RequestMapping(value = "format/uncategorized", method = RequestMethod.GET)
    public String showUncategorizedFileList(final Model model, final AppUser appUser) {
        return ViewPath.FILE_LIST_VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFilesGroupedByCategory(final Model model, final AppUser appUser) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);

        fileManagementService.syncFilesWithDatabase(userAccount);

        model.addAttribute("items", fileService.countGroupByFileFormat(userAccount));

        return ViewPath.FILE_VIEW;
    }

}
