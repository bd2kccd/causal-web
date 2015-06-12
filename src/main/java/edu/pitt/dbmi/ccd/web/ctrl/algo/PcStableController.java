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
package edu.pitt.dbmi.ccd.demo.ctrl.algo;

import edu.pitt.dbmi.ccd.demo.model.PcStableRunInfo;
import edu.pitt.dbmi.ccd.demo.service.AlgorithmService;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * Apr 4, 2015 8:09:20 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "/algorithm/pcstable")
public class PcStableController extends AlgorithmController {

    private final String pcStable;

    private final String cmd;

    @Autowired
    private AlgorithmService algorithmService;

    @Autowired(required = true)
    public PcStableController(
            @Value(value = "#{app.pcStableApp}") String pcStable,
            @Value(value = "#{app.uploadDir}") String uploadDirectory,
            @Value(value = "#{app.libDir}") String libDirectory,
            @Value(value = "#{app.outputDir}") String outputDirectory,
            @Value(value = "#{app.tempDir}") String tempDirectory,
            @Value(value = "#{app.algoJar}") String algorithmJar) {
        super(uploadDirectory, libDirectory, outputDirectory, tempDirectory, algorithmJar);
        this.pcStable = pcStable;

        String classPath = libDirectory + File.separator + algorithmJar;
        this.cmd = String.format("java -cp %s %s", classPath, pcStable);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showPcStableView(Model model) {
        PcStableRunInfo info = new PcStableRunInfo();
        info.setAlpha(0.0001D);
        info.setDepth(3);
        info.setContinuous(Boolean.TRUE);
        info.setVerbose(Boolean.TRUE);
        model.addAttribute("pcStableRunInfo", info);

        model.addAttribute("dataset", directoryFileListing(uploadDirectory));

        return "pcStable";
    }

    @RequestMapping(method = RequestMethod.POST)
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

        return "algorithmRunning";
    }

}
