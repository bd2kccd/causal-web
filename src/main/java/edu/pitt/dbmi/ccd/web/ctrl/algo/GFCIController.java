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
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmRunInfo;
import edu.pitt.dbmi.ccd.web.model.algo.GfciContinuousRunInfo;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;
import edu.pitt.dbmi.ccd.web.util.CmdOptions;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

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

    private final AlgorithmService algorithmService;
    private final CcdProperties ccdProperties;

    @Autowired
    public GFCIController(AlgorithmService algorithmService, CcdProperties ccdProperties) {
        this.algorithmService = algorithmService;
        this.ccdProperties = ccdProperties;
    }

    @RequestMapping(value = "cont", method = RequestMethod.GET)
    public String showGfciContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
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

        return GFCI_CONT_VIEW;
    }

    @RequestMapping(value = "cont", method = RequestMethod.POST)
    public String runGfciContinuous(
            @ModelAttribute("algoInfo") final GfciContinuousRunInfo algoInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCIc", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfciCont());
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
        parameters.add(CmdOptions.DELIMITER);
        parameters.add(delimiter);
        parameters.add(CmdOptions.ALPHA);
        parameters.add(Double.toString(algoInfo.getAlpha()));
        parameters.add(CmdOptions.PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoInfo.getPenaltyDiscount()));
        parameters.add(CmdOptions.MAX_DEGREE);
        parameters.add(Integer.toString(algoInfo.getMaxDegree()));
        if (algoInfo.isVerbose()) {
            parameters.add(CmdOptions.VERBOSE);
        }
        if (algoInfo.isFaithfulnessAssumed()) {
            parameters.add(CmdOptions.FAITHFULNESS_ASSUMED);
        }
        if (algoInfo.isSkipNonzeroVariance()) {
            parameters.add(CmdOptions.SKIP_NONZERO_VARIANCE);
        }
        if (algoInfo.isSkipUniqueVarName()) {
            parameters.add(CmdOptions.SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(CmdOptions.TETRAD_GRAPH_JSON);

        return parameters;
    }

    private GfciContinuousRunInfo createDefaultGfciContinuousRunInfo() {
        GfciContinuousRunInfo runInfo = new GfciContinuousRunInfo();
        runInfo.setAlpha(CmdOptions.ALPHA_DEFAULT);
        runInfo.setPenaltyDiscount(CmdOptions.PENALTY_DISCOUNT_DEFAULT);
        runInfo.setMaxDegree(CmdOptions.MAX_DEGREE_DEFAULT);
        runInfo.setFaithfulnessAssumed(CmdOptions.FAITHFULNESS_ASSUMED_DEFAULT);
        runInfo.setSkipUniqueVarName(CmdOptions.SKIP_UNIQUE_VAR_NAME_DEFAULT);
        runInfo.setSkipNonzeroVariance(CmdOptions.SKIP_NONZERO_VARIANCE_DEFAULT);
        runInfo.setVerbose(CmdOptions.VERBOSE_DEFAULT);
        runInfo.setJvmMaxMem(1);

        return runInfo;
    }

}
