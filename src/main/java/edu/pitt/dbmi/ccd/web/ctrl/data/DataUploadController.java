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

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.data.ResumableChunk;
import edu.pitt.dbmi.ccd.web.service.data.DataFileManagerService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Aug 6, 2015 2:19:19 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "data/upload")
public class DataUploadController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataUploadController.class);

    private final DataFileManagerService fileManager;

    @Autowired(required = true)
    public DataUploadController(DataFileManagerService fileManager) {
        this.fileManager = fileManager;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDataUploadView() {
        return DATA_UPLOAD_VIEW;
    }

    @RequestMapping(value = "chunk", method = RequestMethod.GET)
    public void checkChunkExistence(HttpServletResponse response, ResumableChunk chunk, @ModelAttribute("appUser") AppUser appUser) throws IOException {
        if (fileManager.chunkExists(chunk, appUser)) {
            response.setStatus(200); // do not upload chunk again
        } else {
            response.setStatus(404); // chunk not on the server, upload it
        }
    }

    @RequestMapping(value = "chunk", method = RequestMethod.POST)
    public void processChunkUpload(HttpServletResponse response, ResumableChunk chunk, @ModelAttribute("appUser") AppUser appUser) throws IOException {
        if (!fileManager.isSupported(chunk)) {
            response.setStatus(501); // cancel the whole upload
            return;
        }

        try {
            fileManager.storeChunk(chunk, appUser);
            if (fileManager.allChunksUploaded(chunk, appUser)) {
                response.getWriter().println(fileManager.mergeDeleteSave(chunk, appUser));
            }
        } catch (IOException exception) {
            LOGGER.error(
                    String.format("Unable to upload chunk %s.", chunk.getResumableFilename()),
                    exception);
            throw exception;
        }
        response.setStatus(200);
    }

}
