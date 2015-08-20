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

import edu.pitt.dbmi.ccd.db.service.DataFileInfoService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.GesRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Jun 15, 2015 9:01:24 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/ges")
public class GesController extends AbstractAlgorithmController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(GesController.class);

    private final String ges;

    private final AlgorithmService algorithmService;

    @Autowired(required = true)
    public GesController(
            @Value("${ccd.algorithm.ges:edu.pitt.dbmi.ccd.algorithm.tetrad.FastGesApp}") String ges,
            AlgorithmService algorithmService,
            @Value("${ccd.algorithm.jar:ccd-algorithm.jar}") String algorithmJar,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            DataFileService dataFileService,
            DataFileInfoService dataFileInfoService,
            DataService dataService) {
        super(algorithmJar, variableTypeService, fileDelimiterService, dataFileService, dataFileInfoService, dataService);
        this.ges = ges;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showGesView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        GesRunInfo info = new GesRunInfo();
        info.setPenaltyDiscount(2.0);
        info.setDepth(3);
        info.setVerbose(Boolean.TRUE);
        info.setJvmOptions("");
        info.setRunOnPsc(Boolean.FALSE);

        Map<String, String> map = directoryFileListing(appUser.getDataDirectory(), appUser.getUsername());
        if (map.isEmpty()) {
            info.setDataset("");
        } else {
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                info.setDataset(key);
                break;
            }
        }

        model.addAttribute("datasetList", map);
        model.addAttribute("algoInfo", info);

        return GES_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runPcStable(
            @ModelAttribute("algoInfo") final GesRunInfo info,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        List<String> commands = new LinkedList<>();
        commands.add("java");

        String jvmOptions = info.getJvmOptions().trim();
        if (jvmOptions.length() > 0) {
            commands.addAll(Arrays.asList(jvmOptions.split("\\s+")));
        }

        Path classPath = Paths.get(appUser.getLibDirectory(), algorithmJar);
        commands.add("-cp");
        commands.add(classPath.toString());
        commands.add(ges);

        Path dataset = Paths.get(appUser.getDataDirectory(), info.getDataset());
        commands.add("--data");
        commands.add(dataset.toString());

        commands.add("--delimiter");
        commands.add(getFileDelimiter(appUser.getDataDirectory(), info.getDataset()));

        commands.add("--penalty-discount");
        commands.add(String.valueOf(info.getPenaltyDiscount().doubleValue()));

        commands.add("--depth");
        commands.add(String.valueOf(info.getDepth().intValue()));

        if (info.getVerbose()) {
            commands.add("--verbose");
        }

        String fileName = String.format("ges_%s_%d", info.getDataset(), System.currentTimeMillis());
        commands.add("--out-filename");
        commands.add(fileName);

        try {
            algorithmService.runAlgorithm(commands, fileName, appUser.getTmpDirectory(), appUser.getResultDirectory());
        } catch (Exception exception) {
            LOGGER.error("Unable to run GES.", exception);
        }

        model.addAttribute("title", "GES is Running");

        return ALGO_RUN_CONFIRM_VIEW;
    }

}
