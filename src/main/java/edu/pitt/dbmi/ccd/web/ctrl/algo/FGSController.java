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

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.model.algo.FgsRunInfo;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Nov 17, 2015 11:42:14 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/fgs")
public class FGSController implements ViewPath {

    private final static String ALGORITHM_NAME = "fgs";

    private final String algorithm;

    protected final String algorithmJar;

    private final AlgorithmService algorithmService;

    @Autowired(required = true)
    public FGSController(
            @Value("${ccd.algorithm.fgs}") String algorithm,
            @Value("${ccd.jar.algorithm}") String algorithmJar,
            AlgorithmService algorithmService) {
        this.algorithm = algorithm;
        this.algorithmJar = algorithmJar;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFgsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        FgsRunInfo info = new FgsRunInfo();
        info.setPenaltyDiscount(4.0);
        info.setDepth(3);
        info.setVerbose(Boolean.TRUE);
        info.setJvmOptions("");

        Map<String, String> map = algorithmService.getUserDataset(appUser.getUsername());
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

        return FGS_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runFgs(
            @ModelAttribute("algoInfo") final FgsRunInfo info,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {

        // build the parameters
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(info.getDataset(), appUser.getUsername());
        parameters.add("--delimiter");
        parameters.add(delimiter);
        parameters.add("--penalty-discount");
        parameters.add(String.valueOf(info.getPenaltyDiscount().doubleValue()));
        parameters.add("--depth");
        parameters.add(String.valueOf(info.getDepth().intValue()));
        if (info.isVerbose()) {
            parameters.add("--verbose");
        }

        List<String> jvmOptions = new LinkedList<>();
        String jvmOpts = info.getJvmOptions().trim();
        if (jvmOpts.length() > 0) {
            jvmOptions.addAll(Arrays.asList(jvmOpts.split("\\s+")));
        }

        List<String> dataset = new LinkedList<>();
        dataset.add(info.getDataset());

        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest(ALGORITHM_NAME, algorithmJar, algorithm);
        jobRequest.setDataset(dataset);
        jobRequest.setJvmOptions(jvmOptions);
        jobRequest.setParameters(parameters);

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

}
