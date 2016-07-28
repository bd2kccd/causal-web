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
@RequestMapping(value = "algorithm/causal/fgs")
public class FGSController implements ViewPath {

    @RequestMapping(value = "fgsc", method = RequestMethod.GET)
    public String showFgsContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return HOME_VIEW;
    }

    @RequestMapping(value = "fgsd", method = RequestMethod.GET)
    public String showFgsDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        return HOME_VIEW;
    }

}
