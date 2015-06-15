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
import static edu.pitt.dbmi.ccd.web.ctrl.ViewController.ALGORITHM_RUNNING;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.GesRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import edu.pitt.dbmi.ccd.web.service.FileInfoService;
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
 * Jun 15, 2015 9:01:24 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/ges")
public class GesController extends AlgorithmController implements ViewController {

    private final String ges;

    @Autowired
    private AlgorithmService algorithmService;

    @Autowired(required = true)
    private FileInfoService fileInfoService;

    @Autowired(required = true)
    public GesController(
            @Value("${app.gesApp:edu.pitt.dbmi.ccd.algorithm.tetrad.GesApp}") String ges,
            @Value("${app.uploadDir}") String uploadDirectory,
            @Value("${app.libDir}") String libDirectory,
            @Value("${app.outputDir}") String outputDirectory,
            @Value("${app.tempDir}") String tempDirectory,
            @Value("${app.algoJar:ccd-algorithm-1.0-SNAPSHOT.jar}") String algorithmJar) {
        super(uploadDirectory, libDirectory, outputDirectory, tempDirectory, algorithmJar);
        this.ges = ges;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showGesView(Model model, @ModelAttribute("appUser") AppUser appUser) {
        GesRunInfo gesRunInfo = new GesRunInfo();
        gesRunInfo.setExcludeZeroCorrelationEdges(Boolean.TRUE);
        gesRunInfo.setPenaltyDiscount(2.0);
        gesRunInfo.setContinuous(Boolean.TRUE);
        gesRunInfo.setVerbose(Boolean.TRUE);
        model.addAttribute("gesRunInfo", gesRunInfo);

        String baseDir = appUser.getPerson().getWorkspaceDirectory();
        model.addAttribute("dataset", directoryFileListing(Paths.get(baseDir, uploadDirectory)));

        return GES;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runGes(Model model,
            @ModelAttribute("gesRunInfo") GesRunInfo info,
            @ModelAttribute("appUser") AppUser appUser) {

        String baseDir = appUser.getPerson().getWorkspaceDirectory();
        Path classPath = Paths.get(baseDir, libDirectory, algorithmJar);
        String cmd = String.format("java -cp %s %s", classPath.toString(), ges);
        StringBuilder cmdBuilder = new StringBuilder(cmd);

        Path dataset = Paths.get(baseDir, uploadDirectory, info.getDataset());
        cmdBuilder.append(" --data ");
        cmdBuilder.append(dataset.toString());

        // continuous variables
        cmdBuilder.append(" --continuous");

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
        try {
            algorithmService.runAlgorithm(cmdBuilder.toString(), fileName, baseDir);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }

        model.addAttribute("title", "GES is Running");

        return ALGORITHM_RUNNING;
    }

}
