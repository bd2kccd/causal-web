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
import edu.pitt.dbmi.ccd.web.model.algo.PcStableRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import edu.pitt.dbmi.ccd.web.service.DataService;
import edu.pitt.dbmi.ccd.web.service.cloud.dto.JobRequest;
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
 * Apr 4, 2015 8:09:20 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/pcStable")
public class PcStableController extends AbstractAlgorithmController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(PcStableController.class);

    private final String pcStable;

    private final AlgorithmService algorithmService;

    @Autowired(required = true)
    public PcStableController(
            @Value("${ccd.algorithm.pcstable:edu.pitt.dbmi.ccd.algorithm.tetrad.PcStableApp}") String pcStable,
            AlgorithmService algorithmService,
            @Value("${ccd.algorithm.jar:ccd-algorithm.jar}") String algorithmJar,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            DataFileService dataFileService,
            DataFileInfoService dataFileInfoService,
            DataService dataService) {
        super(algorithmJar, variableTypeService, fileDelimiterService, dataFileService, dataFileInfoService, dataService);
        this.pcStable = pcStable;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showPcStableView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        PcStableRunInfo info = new PcStableRunInfo();
        info.setAlpha(0.0001D);
        info.setDepth(3);
        info.setVerbose(Boolean.TRUE);
        info.setJvmOptions("");
        info.setRunOnPsc(Boolean.FALSE);

        Map<String, String> map = directoryFileListing(appUser.getUsername());
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

        return PC_STABLE_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runPcStable(
            @ModelAttribute("algoInfo") final PcStableRunInfo info,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {

        List<String> algoOptions = new LinkedList<>();
        algoOptions.add("--alpha");
        algoOptions.add(String.valueOf(info.getAlpha().doubleValue()));

        algoOptions.add("--depth");
        algoOptions.add(String.valueOf(info.getDepth().intValue()));

        if (info.getVerbose()) {
            algoOptions.add("--verbose");
        }

        String[] jvmOptions = null;
        String jvmOpts = info.getJvmOptions().trim();
        if (jvmOpts.length() > 0) {
            jvmOptions = jvmOpts.split("\\s+");
        }

        JobRequest jobRequest = new JobRequest();
        jobRequest.setAlgorName("pcstable");
        jobRequest.setJvmOptions(jvmOptions);
        jobRequest.setAlgoParams(algoOptions.toArray(new String[algoOptions.size()]));
        jobRequest.setDataset(info.getDataset());

        if (info.getRunOnPsc()) {
            algorithmService.runRemotely(jobRequest, appUser);
        } else {
            algorithmService.runLocally(pcStable, algorithmJar, jobRequest, appUser);
        }

        return REDIRECT_JOB_QUEUE;
    }

}
