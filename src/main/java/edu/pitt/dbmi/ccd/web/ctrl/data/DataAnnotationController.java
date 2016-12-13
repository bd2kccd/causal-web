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

import edu.pitt.dbmi.ccd.db.entity.AnnotationTarget;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Nov 22, 2016 5:20:19 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("annotations")
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "data/annotations")
public class DataAnnotationController implements ViewPath {

    private final DataService dataService;

    @Autowired
    public DataAnnotationController(DataService dataService) {
        this.dataService = dataService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDataFileAnnotations(
            @RequestParam(value = "fileName") final String fileName,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        String username = appUser.getUsername();
        model.addAttribute("username", username);
        model.addAttribute("fileName", fileName);
        AnnotationTarget target = dataService.getAnnotationTarget(fileName, username);
        if (target != null) {
            model.addAttribute("annotationTargetID", target.getId());
        }

        return DATA_ANNOTATIONS_VIEW;
    }

}
