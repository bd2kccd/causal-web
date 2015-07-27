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
package edu.pitt.dbmi.ccd.web.ctrl.data;

import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResumableChunk;
import edu.pitt.dbmi.ccd.web.service.BigDataFileManager;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Apr 3, 2015 11:04:04 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/data")
public class DataController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    private final BigDataFileManager fileManager;

    private final DataService dataService;

    @Autowired(required = true)
    public DataController(BigDataFileManager fileManager, DataService dataService) {
        this.fileManager = fileManager;
        this.dataService = dataService;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String showDataUploadView() {
        return DATA_UPLOAD;
    }

    @RequestMapping(value = "/upload/chunk", method = RequestMethod.GET)
    public void checkChunkExistence(HttpServletResponse response, ResumableChunk chunk, @ModelAttribute("appUser") AppUser appUser) throws IOException {
        if (fileManager.chunkExists(chunk.getResumableIdentifier(), chunk.getResumableChunkNumber(), chunk.getResumableChunkSize(), appUser.getUploadDirectory())) {
            response.setStatus(200); // do not upload chunk again
        } else {
            response.setStatus(404); // chunk not on the server, upload it
        }
    }

    @RequestMapping(value = "/upload/chunk", method = RequestMethod.POST)
    public void processChunkUpload(HttpServletResponse response, ResumableChunk chunk, @ModelAttribute("appUser") AppUser appUser) throws IOException {
        if (!fileManager.isSupported(chunk.getResumableFilename())) {
            response.setStatus(501); // cancel the whole upload
            return;
        }

        try {
            fileManager.storeChunk(chunk.getResumableIdentifier(), chunk.getResumableChunkNumber(), chunk.getFile().getInputStream(), appUser.getUploadDirectory());
            if (fileManager.allChunksUploaded(chunk.getResumableIdentifier(), chunk.getResumableChunkSize(), chunk.getResumableTotalSize(), chunk.getResumableTotalChunks(), appUser.getUploadDirectory())) {
                String md5 = fileManager.mergeAndDeleteWithMd5(chunk.getResumableFilename(), chunk.getResumableIdentifier(), chunk.getResumableChunkSize(), chunk.getResumableTotalSize(), chunk.getResumableTotalChunks(), appUser.getUploadDirectory());

//                Path path = Paths.get(appUser.getUploadDirectory(), chunk.getResumableFilename());
//                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                response.getWriter().println(md5);
            }
        } catch (IOException exception) {
            LOGGER.error(
                    String.format("Unable to upload chunk %s.", chunk.getResumableFilename()),
                    exception);
            throw exception;
        }
        response.setStatus(200);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDatasetView(Model model, @ModelAttribute("appUser") AppUser appUser) {
        model.addAttribute("itemList", dataService.createListItem(appUser.getUsername(), appUser.getUploadDirectory()));

        return DATASET;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteResultFile(@RequestParam(value = "file") String filename, Model model, @ModelAttribute("appUser") AppUser appUser) {
        String dataDir = appUser.getUploadDirectory();
        Path file = Paths.get(dataDir, filename);
        if (dataService.getDataFileService().deleteDataFileByNameAndAbsolutePath(dataDir, filename)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {
                LOGGER.error(
                        String.format("Unable to delete file %s.", file.toAbsolutePath().toString()),
                        exception);
            }
        }

        return "redirect:/data";
    }

    @RequestMapping(value = "/example", method = RequestMethod.GET)
    public String getFileFInfo(@RequestParam(value = "type") String type) {
        return type;
    }

}
