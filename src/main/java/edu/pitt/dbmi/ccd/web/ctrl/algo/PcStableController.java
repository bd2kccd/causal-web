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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(PcStableController.class);

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
        String[] dataTypes = {"continuous", "discrete"};
        String[] delimiters = {"tab", "comma"};

        PcStableRunInfo info = new PcStableRunInfo();
        info.setAlpha(0.0001D);
        info.setDepth(3);
        info.setDataType(dataTypes[0]);
        info.setDelimiter(delimiters[0]);
        info.setVerbose(Boolean.TRUE);
        info.setJvmOptions("");
        model.addAttribute("pcStableRunInfo", info);

        model.addAttribute("datasetList", directoryFileListing(Paths.get(appUser.getUploadDirectory())));
        model.addAttribute("dataTypes", Arrays.asList(dataTypes));
        model.addAttribute("delimiters", Arrays.asList(delimiters));

        return PCSTABLE;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runPcStable(Model model,
            @ModelAttribute("pcStableRunInfo") PcStableRunInfo info,
            @ModelAttribute("appUser") AppUser appUser) {
        List<String> commands = new LinkedList<>();
        commands.add("java");

        String jvmOptions = info.getJvmOptions().trim();
        if (jvmOptions.length() > 0) {
            commands.addAll(Arrays.asList(jvmOptions.split("\\s+")));
        }

        Path classPath = Paths.get(appUser.getLibDirectory(), algorithmJar);
        commands.add("-cp");
        commands.add(classPath.toString());
        commands.add(pcStable);

        Path dataset = Paths.get(appUser.getUploadDirectory(), info.getDataset());
        commands.add("--data");
        commands.add(dataset.toString());

        if ("comma".equals(info.getDelimiter())) {
            commands.add("--delimiter");
            commands.add(",");
        }

        commands.add("--alpha");
        commands.add(String.valueOf(info.getAlpha().doubleValue()));

        commands.add("--depth");
        commands.add(String.valueOf(info.getDepth().intValue()));

        if (info.getVerbose()) {
            commands.add("--verbose");
        }

        String fileName = String.format("pc-stable_%s_%d", info.getDataset(), System.currentTimeMillis());
        commands.add("--out-filename");
        commands.add(fileName);

        try {
            algorithmService.runAlgorithm(commands, fileName, appUser.getTmpDirectory(), appUser.getOutputDirectory());
        } catch (Exception exception) {
            LOGGER.error("Unable to run GES.", exception);
        }

        model.addAttribute("title", "PC-Stable is Running");

        return ALGORITHM_RUNNING;
    }

}
