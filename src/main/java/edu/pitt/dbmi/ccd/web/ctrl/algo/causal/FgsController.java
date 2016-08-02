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
package edu.pitt.dbmi.ccd.web.ctrl.algo.causal;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.algo.FgscRunInfo;
import edu.pitt.dbmi.ccd.web.service.algo.causal.FgsService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 16, 2016 9:14:48 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/secured/algorithm/causal/fgs")
public class FgsController implements ViewPath {

    private final FgsService fgsService;

    @Autowired
    public FgsController(FgsService fgsService) {
        this.fgsService = fgsService;
    }

    @RequestMapping(value = "fgsc", method = RequestMethod.GET)
    public String showFgsContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        fgsService.showFgsContinuousView(appUser, model);

        return FGS_CONTINUOUS_VIEW;
    }

    @RequestMapping(value = "fgsc", method = RequestMethod.POST)
    public String runFgsContinuous(
            @Valid @ModelAttribute("fgscRunInfo") final FgscRunInfo fgscRunInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        System.out.println("================================================================================");
        System.out.println(fgscRunInfo);
        System.out.println("================================================================================");

        return FGS_CONTINUOUS_VIEW;
    }

    @RequestMapping(value = "fgsd", method = RequestMethod.GET)
    public String showFgsDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return HOME_VIEW;
    }

}
