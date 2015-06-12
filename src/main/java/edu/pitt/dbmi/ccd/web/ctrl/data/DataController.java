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
package edu.pitt.dbmi.ccd.demo.ctrl.data;

import edu.pitt.dbmi.ccd.demo.model.ResumableChunk;
import edu.pitt.dbmi.ccd.demo.service.BigDataFileManager;
import edu.pitt.dbmi.ccd.demo.util.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * Apr 3, 2015 11:04:04 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "/data")
public class DataController {

    private final BigDataFileManager fileManager;

    @Autowired(required = true)
    public DataController(BigDataFileManager fileManager) {
        this.fileManager = fileManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDatasetView(Model model) {
        model.addAttribute("itemList", FileUtility.getFileListing(fileManager.getUploadDirectory()));

        return "dataset";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteResultFile(@RequestParam(value = "file") String filename, Model model) {
        Path file = Paths.get(fileManager.getUploadDirectory(), filename);
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        return "redirect:/data";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String showDataUploadView() {
        return "dataUpload";
    }

    @RequestMapping(value = "/upload/chunk", method = RequestMethod.GET)
    public void checkChunkExistence(HttpServletResponse response, ResumableChunk chunk) throws IOException {
        if (fileManager.chunkExists(chunk.getResumableIdentifier(), chunk.getResumableChunkNumber(), chunk.getResumableChunkSize())) {
            response.setStatus(200); // do not upload chunk again
        } else {
            response.setStatus(404); // chunk not on the server, upload it
        }
    }

    @RequestMapping(value = "/upload/chunk", method = RequestMethod.POST)
    public void processChunkUpload(HttpServletResponse response, ResumableChunk chunk) throws IOException {
        if (!fileManager.isSupported(chunk.getResumableFilename())) {
            response.setStatus(501); // cancel the whole upload
            return;
        }
        fileManager.storeChunk(chunk.getResumableIdentifier(), chunk.getResumableChunkNumber(), chunk.getFile().getInputStream());
        if (fileManager.allChunksUploaded(chunk.getResumableIdentifier(), chunk.getResumableChunkSize(), chunk.getResumableTotalSize(), chunk.getResumableTotalChunks())) {
            String md5 = fileManager.mergeAndDeleteWithMd5(chunk.getResumableFilename(), chunk.getResumableIdentifier(), chunk.getResumableChunkSize(), chunk.getResumableTotalSize(), chunk.getResumableTotalChunks());
            response.getWriter().println(md5);
        }
        response.setStatus(200);
    }

}
