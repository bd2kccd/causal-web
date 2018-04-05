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
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.file.ResumableChunk;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.file.FileUploadService;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import java.io.IOException;
import java.nio.file.Path;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Apr 25, 2017 6:21:13 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/file/upload")
public class FileUploadController {

    private static final int CHUNK_EXIST_STATUS = 200;
    private static final int CHUNK_NOT_EXIST_STATUS = 404;
    private static final int FAIL_STATUS = 501;

    private final AppUserService appUserService;
    private final FileUploadService fileUploadService;
    private final FileService fileService;

    @Autowired
    public FileUploadController(AppUserService appUserService, FileUploadService fileUploadService, FileService fileService) {
        this.appUserService = appUserService;
        this.fileUploadService = fileUploadService;
        this.fileService = fileService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDataUploadView() {
        return ViewPath.FILE_UPLOAD_VIEW;
    }

    @RequestMapping(value = "chunk", method = RequestMethod.POST)
    public void processChunkUpload(ResumableChunk chunk, AppUser appUser, HttpServletResponse res) throws IOException {
        if (fileUploadService.isSupported(chunk)) {
            UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
            if (fileUploadService.saveChunk(chunk, userAccount)) {
                if (fileUploadService.allChunksUploaded(chunk, userAccount)) {
                    Path uploadedFile = fileUploadService.combineAllChunks(chunk, userAccount);
                    if (uploadedFile == null) {
                        res.getWriter().println("Upload failed.");
                        res.setStatus(FAIL_STATUS);
                    } else {
                        File fileEntity = fileService.persistLocalFile(uploadedFile, userAccount);
                        res.getWriter().println(fileEntity.getMd5CheckSum());
                    }
                }
            } else {
                res.getWriter().println("Upload failed.");
                res.setStatus(FAIL_STATUS);
            }
        } else {
            res.getWriter().println("File not supported.");
            res.setStatus(FAIL_STATUS); // cancel the whole upload
        }

    }

    @RequestMapping(value = "chunk", method = RequestMethod.GET)
    public void checkChunkExistence(ResumableChunk chunk, @ModelAttribute("appUser") AppUser appUser, HttpServletResponse res) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount != null && fileUploadService.chunkExists(chunk, userAccount)) {
            res.setStatus(CHUNK_EXIST_STATUS);
        } else {
            res.setStatus(CHUNK_NOT_EXIST_STATUS);
        }
    }

}
