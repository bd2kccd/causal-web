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
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.PcStableRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Apr 4, 2015 8:09:20 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/pcStable")
public class PcStableController extends AlgorithmController implements ViewController {

    private final String pcStable;

    final private AlgorithmService algorithmService;

    @Autowired(required = true)
    public PcStableController(
            @Value("${app.pcStableApp:edu.pitt.dbmi.ccd.algorithm.tetrad.PcStableApp}") String pcStable,
            AlgorithmService algorithmService,
            @Value("${app.algoJar:ccd-algorithm-1.0-SNAPSHOT.jar}") String algorithmJar) {
        super(algorithmJar);
        this.pcStable = pcStable;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showPcStableView(Model model, @ModelAttribute("appUser") AppUser appUser) {
        PcStableRunInfo info = new PcStableRunInfo();
        info.setAlpha(0.0001D);
        info.setDepth(3);
        info.setContinuous(Boolean.TRUE);
        info.setVerbose(Boolean.TRUE);
        model.addAttribute("pcStableRunInfo", info);

        model.addAttribute("dataset", directoryFileListing(Paths.get(appUser.getUploadDirectory())));

        return PCSTABLE;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runPcStable(Model model,
            @ModelAttribute("pcStableRunInfo") PcStableRunInfo info,
            @ModelAttribute("appUser") AppUser appUser) {

        Path classPath = Paths.get(appUser.getLibDirectory(), algorithmJar);
        String cmd = String.format("java -cp %s %s", classPath.toString(), pcStable);
        StringBuilder cmdBuilder = new StringBuilder(cmd);

        Path dataset = Paths.get(appUser.getUploadDirectory(), info.getDataset());
        cmdBuilder.append(" --data ");
        cmdBuilder.append(dataset.toString());

        // continuous variables
        cmdBuilder.append(" --continuous");

        cmdBuilder.append(" --alpha ");
        cmdBuilder.append(info.getAlpha());

        cmdBuilder.append(" --depth ");
        cmdBuilder.append(info.getDepth());

        if (info.getVerbose()) {
            cmdBuilder.append(" --verbose");
        }

        String fileName = String.format("pc-stable_%s_%d.txt", info.getDataset(), System.currentTimeMillis());
        cmdBuilder.append(" --fileName ");
        cmdBuilder.append(fileName);

        // run the algorithm
        try {
            algorithmService.runAlgorithm(cmdBuilder.toString(), fileName, appUser.getTmpDirectory(), appUser.getOutputDirectory());
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }

        model.addAttribute("title", "PC-Stable is Running");

        return ALGORITHM_RUNNING;
    }

}
