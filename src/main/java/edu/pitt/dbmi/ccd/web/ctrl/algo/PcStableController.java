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

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import edu.pitt.dbmi.ccd.web.model.PcStableRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;

/**
 *
 * May 21, 2015 2:58:35 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 * 
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm")
public class PcStableController extends AlgorithmController implements
		ViewController {

    private final String pcStable;

    private final String cmd;

    @Autowired
    private AlgorithmService algorithmService;

    /**
	 * @param uploadDirectory
	 * @param libDirectory
	 * @param outputDirectory
	 * @param tempDirectory
	 * @param algorithmJar
	 */
    @Autowired(required = true)
    public PcStableController(
            @Value("${app.pcStableApp:edu.pitt.dbmi.ccd.algorithm.tetrad.PcStableApp}") String pcStable,
            @Value("${app.uploadDir}") String uploadDirectory,
            @Value("${app.libDir}") String libDirectory,
            @Value("${app.outputDir}") String outputDirectory,
            @Value("${app.tempDir}") String tempDirectory,
            @Value("${app.algoJar:ccd-algorithm-1.0-SNAPSHOT.jar}") String algorithmJar) {
        super(uploadDirectory, libDirectory, outputDirectory, tempDirectory, algorithmJar);
        
        this.pcStable = pcStable;

        String classPath = libDirectory + File.separator + algorithmJar;
        this.cmd = String.format("java -cp %s %s", classPath, pcStable);
	}

	@RequestMapping(value = PCSTABLE, method = RequestMethod.GET)
    public String showPcStableView(Model model) {
        PcStableRunInfo info = new PcStableRunInfo();
        info.setAlpha(0.0001D);
        info.setDepth(3);
        info.setContinuous(Boolean.TRUE);
        info.setVerbose(Boolean.TRUE);
        model.addAttribute("pcStableRunInfo", info);

        model.addAttribute("dataset", directoryFileListing(uploadDirectory));


        return PCSTABLE;
    }

    @RequestMapping(value = PCSTABLE, method = RequestMethod.POST)
    public String runPcStable(
            Model model,
            @ModelAttribute("pcStableRunInfo") PcStableRunInfo info) throws Exception {
        StringBuilder cmdBuilder = new StringBuilder(cmd);

        cmdBuilder.append(" --data ");
        cmdBuilder.append(uploadDirectory);
        cmdBuilder.append(File.separator);
        cmdBuilder.append(info.getDataset());

        // continuous variables
        cmdBuilder.append(" --continuous");
//        if (info.getContinuous()) {
//            cmdBuilder.append(" --continuous");
//        }

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
        algorithmService.runAlgorithm(cmdBuilder.toString(), fileName);

        model.addAttribute("title", "PC-Stable is Running");

        return ALGORITHMRUNNING;
    }
	
}
