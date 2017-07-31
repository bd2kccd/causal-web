/*
 * Copyright (C) 2017 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.ctrl.algorithm;

import edu.pitt.dbmi.ccd.db.service.FileVariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algorithm.GfcicJobForm;
import edu.pitt.dbmi.ccd.web.model.algorithm.GfcidJobForm;
import edu.pitt.dbmi.ccd.web.model.algorithm.GfcimJobForm;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.algorithm.TetradJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Jul 27, 2017 5:13:54 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/algorithm/causal-discover/gfci")
public class GFCIController extends AbstractTetradController implements ViewPath {

    private final String gfcic;
    private final String gfcid;
    private final String gfcim;

    @Autowired
    public GFCIController(
            @Value("${tetrad.algo.gfci.gfcic}") String gfcic,
            @Value("${tetrad.algo.gfci.gfcid}") String gfcid,
            @Value("${tetrad.algo.gfci.gfcim}") String gfcim,
            TetradJobService tetradJobService, AppUserService appUserService) {
        super(tetradJobService, appUserService);
        this.gfcic = gfcic;
        this.gfcid = gfcid;
        this.gfcim = gfcim;
    }

    @RequestMapping(value = "${tetrad.algo.gfci.gfcim}", method = RequestMethod.GET)
    public String showGFCImView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        setupView(new GfcimJobForm(), FileVariableTypeService.MIXED_NAME, gfcim, appUser, model);

        return GFCIM_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.gfci.gfcid}", method = RequestMethod.GET)
    public String showGFCIdView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        setupView(new GfcidJobForm(), FileVariableTypeService.DISCRETE_NAME, gfcid, appUser, model);

        return GFCID_VIEW;
    }

    @RequestMapping(value = "${tetrad.algo.gfci.gfcic}", method = RequestMethod.GET)
    public String showGFCIcView(@ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        setupView(new GfcicJobForm(), FileVariableTypeService.CONTINUOUS_NAME, gfcic, appUser, model);

        return GFCIC_VIEW;
    }

}
