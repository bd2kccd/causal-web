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

import edu.pitt.dbmi.ccd.web.model.GesRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;

/**
 *
 * May 21, 2015 3:09:26 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 * 
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm")
public class GesController extends AlgorithmController implements
		ViewController {

	private final String ges;

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
    public GesController(
            @Value("${app.gesApp:edu.pitt.dbmi.ccd.algorithm.tetrad.GesApp}") String ges,
            @Value("${app.uploadDir}") String uploadDirectory,
            @Value("${app.libDir}") String libDirectory,
            @Value("${app.outputDir}") String outputDirectory,
            @Value("${app.tempDir}") String tempDirectory,
            @Value("${app.algoJar}") String algorithmJar) {
        super(uploadDirectory, libDirectory, outputDirectory, tempDirectory, algorithmJar);
		// TODO Auto-generated constructor stub
        this.ges = ges;

        String classPath = libDirectory + File.separator + algorithmJar;
        this.cmd = String.format("java -cp %s %s", classPath, ges);
	}

    @RequestMapping(value = GES, method = RequestMethod.GET)
    public String showGesView(Model model) {
        GesRunInfo gesRunInfo = new GesRunInfo();
        gesRunInfo.setExcludeZeroCorrelationEdges(Boolean.TRUE);
        gesRunInfo.setPenaltyDiscount(2.0);
        gesRunInfo.setContinuous(Boolean.TRUE);
        gesRunInfo.setVerbose(Boolean.TRUE);

        model.addAttribute("gesRunInfo", gesRunInfo);
        model.addAttribute("dataset", directoryFileListing(uploadDirectory));

        return GES;
    }

    @RequestMapping(value = GES, method = RequestMethod.POST)
    public String runGes(Model model, @ModelAttribute("gesRunInfo") GesRunInfo info) throws Exception {
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

        cmdBuilder.append(" --penalty-discount ");
        cmdBuilder.append(info.getPenaltyDiscount());

        if (info.getExcludeZeroCorrelationEdges()) {
            cmdBuilder.append(" --exclude-zero-corr-edge");
        }

        if (info.getVerbose()) {
            cmdBuilder.append(" --verbose");
        }

        String fileName = String.format("ges_%s_%d.txt", info.getDataset(), System.currentTimeMillis());
        cmdBuilder.append(" --fileName ");
        cmdBuilder.append(fileName);

        // run the algorithm
        algorithmService.runAlgorithm(cmdBuilder.toString(), fileName);

        model.addAttribute("title", "GES is Running");

        return ALGORITHMRUNNING;
    }

}
