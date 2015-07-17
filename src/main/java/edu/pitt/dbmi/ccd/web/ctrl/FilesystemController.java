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

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 17, 2015 10:48:10 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/fs")
public class FilesystemController implements ViewController {

    @RequestMapping(value = "/fileInfo", method = RequestMethod.GET)
    public String getFileFInfo(
            @RequestParam(value = "file") String filename,
            @ModelAttribute("appUser") AppUser appUser) throws IOException {

        System.out.printf("I'm here: %s\n", filename);

//        Path file = Paths.get(appUser.getUploadDirectory(), filename);
//        try {
//            BasicFileInfo info = FileInfos.basicPathInfo(file);
//        } catch (IOException exception) {
//        }
        return FILE_INFO;
    }

}
