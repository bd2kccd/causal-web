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

import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResumableChunk;
import edu.pitt.dbmi.ccd.web.service.BigDataFileManager;
import edu.pitt.dbmi.ccd.web.service.FileInfoService;
import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
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

    private final BigDataFileManager fileManager;

    private final FileInfoService fileInfoService;

    @Autowired(required = true)
    public DataController(BigDataFileManager fileManager, FileInfoService fileInfoService) {
        this.fileManager = fileManager;
        this.fileInfoService = fileInfoService;
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

        fileManager.storeChunk(chunk.getResumableIdentifier(), chunk.getResumableChunkNumber(), chunk.getFile().getInputStream(), appUser.getUploadDirectory());
        if (fileManager.allChunksUploaded(chunk.getResumableIdentifier(), chunk.getResumableChunkSize(), chunk.getResumableTotalSize(), chunk.getResumableTotalChunks(), appUser.getUploadDirectory())) {
            String md5 = fileManager.mergeAndDeleteWithMd5(chunk.getResumableFilename(), chunk.getResumableIdentifier(), chunk.getResumableChunkSize(), chunk.getResumableTotalSize(), chunk.getResumableTotalChunks(), appUser.getUploadDirectory());

            Path path = Paths.get(appUser.getUploadDirectory(), chunk.getResumableFilename());
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            DataFile dataFile = new DataFile();
            dataFile.setFileName(path.getFileName().toString());
            dataFile.setFileAbsolutePath(path.toAbsolutePath().toString());
            dataFile.setCreationTime(new Date(attrs.creationTime().toMillis()));
            dataFile.setLastAccessTime(new Date(attrs.lastAccessTime().toMillis()));
            dataFile.setLastModifiedTime(new Date(attrs.lastModifiedTime().toMillis()));
            dataFile.setFileSize(attrs.size());
            dataFile.setMd5CheckSum(md5);
            fileInfoService.saveFile(dataFile);

            response.getWriter().println(md5);
        }
        response.setStatus(200);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDatasetView(Model model, @ModelAttribute("appUser") AppUser appUser) {
        model.addAttribute("itemList", FileUtility.getFileListing(Paths.get(appUser.getUploadDirectory())));

        return DATASET;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteResultFile(@RequestParam(value = "file") String filename, Model model, @ModelAttribute("appUser") AppUser appUser) {
        Path file = Paths.get(appUser.getUploadDirectory(), filename);
        fileInfoService.deleteFile(file.toAbsolutePath().toString());
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        return "redirect:/data";
    }

}
