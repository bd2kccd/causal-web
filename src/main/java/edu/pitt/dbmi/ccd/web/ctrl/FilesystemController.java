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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.AttributeValue;
import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Aug 6, 2015 10:27:26 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/fs")
public class FilesystemController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemController.class);

    @RequestMapping(value = FILE_INFO, method = RequestMethod.GET)
    public String getFileFInfo(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) throws IOException {
        List<AttributeValue> basicInfo = new LinkedList<>();
        Path file = Paths.get(appUser.getDataDirectory(), filename);
        try {
            BasicFileInfo info = FileInfos.basicPathInfo(file);
            basicInfo.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            basicInfo.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            basicInfo.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            basicInfo.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        model.addAttribute("basicInfo", basicInfo);

        return FILE_INFO_VIEW;
    }

    @RequestMapping(value = DIR_BROWSER, method = RequestMethod.GET)
    public String browseServerSideDirectory(
            @RequestParam(value = "dir", required = false) String directory,
            final Model model) {
        if (directory == null) {
            directory = System.getProperty("user.home");
        }
        model.addAttribute("itemList", FileUtility.getDirListing(directory));
        model.addAttribute("currDir", directory);

        return DIR_BROWSER_VIEW;
    }

    @RequestMapping(value = DIR_BROWSER, method = RequestMethod.POST)
    public String createNewServerSideDirectory(
            @RequestParam(value = "dir", required = false) String directory,
            @RequestParam(value = "newFolder", required = false) String newFolder,
            final Model model) {
        if (directory == null) {
            directory = System.getProperty("user.home");
        }
        Path path = Paths.get(directory);
        Path newDir = Paths.get(path.toAbsolutePath().toString(), newFolder);
        if (Files.notExists(newDir)) {
            try {
                Files.createDirectories(newDir);
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
            }
        }
        model.addAttribute("itemList", FileUtility.getDirListing(path.toAbsolutePath().toString()));
        model.addAttribute("currDir", path.toAbsolutePath().toString());

        return DIR_BROWSER_VIEW;
    }

}
