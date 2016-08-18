/*
 * Copyright (C) 2016 University of Pittsburgh.
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

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.ResumableChunk;
import edu.pitt.dbmi.ccd.web.service.file.FileUploadService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 2, 2016 10:26:45 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/secured/file/upload")
public class FileUploadController implements ViewPath {

    private final FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFileUpload(@ModelAttribute("appUser") AppUser appUser) {
        return FILE_UPLOAD_VIEW;
    }

    @RequestMapping(value = "chunk", method = RequestMethod.GET)
    public void checkChunkExistence(
            ResumableChunk chunk,
            @ModelAttribute("appUser") AppUser appUser,
            HttpServletResponse response) {
        fileUploadService.checkChunkExistence(chunk, appUser, response);
    }

    @RequestMapping(value = "chunk", method = RequestMethod.POST)
    public void processChunkUpload(
            ResumableChunk chunk,
            @ModelAttribute("appUser") AppUser appUser,
            HttpServletResponse response) throws IOException {
        fileUploadService.processChunkUpload(chunk, appUser, response);
    }

}
