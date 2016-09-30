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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmRunInfo;
import edu.pitt.dbmi.ccd.web.model.algo.GfciContinuousRunInfo;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;

/**
 * 
 * Sep 28, 2016 10:01:10 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti, PhD (chw20@pitt.edu)
 *
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/gfci")
public class GFCIController implements ViewPath {

    private final String gfciAlgorithm;
    protected final String algorithmJar;

    private final AlgorithmService algorithmService;

    @Autowired
    public GFCIController(
            @Value("${ccd.algorithm.gfci}") String gfciAlgorithm,
            @Value("${ccd.jar.algorithm}") String algorithmJar,
            AlgorithmService algorithmService) {
        this.gfciAlgorithm = gfciAlgorithm;
        this.algorithmJar = algorithmJar;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showGfciView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GfciContinuousRunInfo algoInfo = createDefaultGfciContinuousRunInfo();

        // set the default dataset
        if (dataset.isEmpty()) {
            algoInfo.setDataset("");
        } else {
            algoInfo.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (prior.isEmpty()) {
            algoInfo.setPriorKnowledge("");
        } else {
            algoInfo.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoInfo", algoInfo);

        return GFCI_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runGfci(
            @ModelAttribute("algoInfo") final GfciContinuousRunInfo algoInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("gfcic", algorithmJar, gfciAlgorithm);
        jobRequest.setDataset(getDataset(algoInfo));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoInfo));
        jobRequest.setJvmOptions(getJvmOptions(algoInfo));
        jobRequest.setParameters(getParametersForContinuous(algoInfo, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    private List<String> getJvmOptions(AlgorithmRunInfo algoInfo) {
        List<String> jvmOptions = new LinkedList<>();

        int jvmMaxMem = algoInfo.getJvmMaxMem();
        if (jvmMaxMem > 0) {
            jvmOptions.add(String.format("-Xmx%dG", jvmMaxMem));
        }

        return jvmOptions;
    }

    private List<String> getDataset(AlgorithmRunInfo algoInfo) {
        return Collections.singletonList(algoInfo.getDataset());
    }

    private List<String> getPriorKnowledge(AlgorithmRunInfo algoInfo) {
        String priorKnowledge = algoInfo.getPriorKnowledge();
        if (priorKnowledge.trim().length() == 0) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.singletonList(algoInfo.getPriorKnowledge());
        }
    }

    private List<String> getParametersForContinuous(GfciContinuousRunInfo algoInfo, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoInfo.getDataset(), username);
        parameters.add("--delimiter");
        parameters.add(delimiter);
        parameters.add("--alpha");
        parameters.add(Double.toString(algoInfo.getAlpha()));
        parameters.add("--penalty-discount");
        parameters.add(Double.toString(algoInfo.getPenaltyDiscount()));
        parameters.add("--max-indegree");
        parameters.add(Integer.toString(algoInfo.getMaxInDegree()));
        if (algoInfo.isVerbose()) {
            parameters.add("--verbose");
        }
        if (algoInfo.isFaithfulnessAssumed()) {
            parameters.add("--faithfulness-assumed");
        }
        if (!algoInfo.isNonZeroVarianceValidation()) {
            parameters.add("--skip-non-zero-variance");
        }
        if (!algoInfo.isUniqueVarNameValidation()) {
            parameters.add("--skip-unique-var-name");
        }

        return parameters;
    }

    private GfciContinuousRunInfo createDefaultGfciContinuousRunInfo() {
    	GfciContinuousRunInfo runInfo = new GfciContinuousRunInfo();
    	runInfo.setAlpha(0.01);
        runInfo.setPenaltyDiscount(4.0);
        runInfo.setFaithfulnessAssumed(true);
        runInfo.setMaxInDegree(3);
        runInfo.setNonZeroVarianceValidation(true);
        runInfo.setUniqueVarNameValidation(true);
        runInfo.setVerbose(true);
        runInfo.setJvmMaxMem(0);

        return runInfo;
    }
    
}
