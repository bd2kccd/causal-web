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
import edu.pitt.dbmi.ccd.web.model.algo.FgsContinuousRunInfo;
import edu.pitt.dbmi.ccd.web.model.algo.FgsDiscreteRunInfo;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;
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

/**
 *
 * Nov 17, 2015 11:42:14 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/fgs")
public class FGSController implements ViewPath {

    private final String fgsAlgorithm;
    private final String fgsDiscreteAlgorithm;
    protected final String algorithmJar;

    private final AlgorithmService algorithmService;

    @Autowired
    public FGSController(
            @Value("${ccd.algorithm.fgs}") String fgsAlgorithm,
            @Value("${ccd.algorithm.fgs.discrete}") String fgsDiscreteAlgorithm,
            @Value("${ccd.jar.algorithm}") String algorithmJar,
            AlgorithmService algorithmService) {
        this.fgsAlgorithm = fgsAlgorithm;
        this.fgsDiscreteAlgorithm = fgsDiscreteAlgorithm;
        this.algorithmJar = algorithmJar;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(value = "discrete", method = RequestMethod.GET)
    public String showFgsDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserDiscreteDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FgsDiscreteRunInfo algoInfo = createDefaultFgsDiscreteRunInfo();

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

        return FGS_DISCRETE_VIEW;
    }

    @RequestMapping(value = "discrete", method = RequestMethod.POST)
    public String runFgsDiscrete(
            @ModelAttribute("algoInfo") final FgsDiscreteRunInfo algoInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("fgsd", algorithmJar, fgsDiscreteAlgorithm);
        jobRequest.setDataset(getDataset(algoInfo));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoInfo));
        jobRequest.setJvmOptions(getJvmOptions(algoInfo));
        jobRequest.setParameters(getParametersForDiscrete(algoInfo, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFgsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FgsContinuousRunInfo algoInfo = createDefaultFgsContinuousRunInfo();

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

        return FGS_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runFgs(
            @ModelAttribute("algoInfo") final FgsContinuousRunInfo algoInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("fgsc", algorithmJar, fgsAlgorithm);
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

    private List<String> getParametersForDiscrete(FgsDiscreteRunInfo algoInfo, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoInfo.getDataset(), username);
        parameters.add("--delimiter");
        parameters.add(delimiter);

        parameters.add("--structure-prior");
        parameters.add(Double.toString(algoInfo.getStructurePrior()));

        parameters.add("--sample-prior");
        parameters.add(Double.toString(algoInfo.getSamplePrior()));

        parameters.add("--max-degree");
        parameters.add(Integer.toString(algoInfo.getMaxDegree()));
        if (algoInfo.isVerbose()) {
            parameters.add("--verbose");
        }
        if (algoInfo.isFaithfulnessAssumed()) {
            parameters.add("--faithfulness-assumed");
        }
        if (!algoInfo.isUniqueVarNameValidation()) {
            parameters.add("--skip-unique-var-name");
        }
        if (!algoInfo.isLimitNumOfCategory()) {
            parameters.add("--skip-category-limit");
        }

        return parameters;
    }

    private List<String> getParametersForContinuous(FgsContinuousRunInfo algoInfo, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoInfo.getDataset(), username);
        parameters.add("--delimiter");
        parameters.add(delimiter);
        parameters.add("--penalty-discount");
        parameters.add(Double.toString(algoInfo.getPenaltyDiscount()));
        parameters.add("--max-degree");
        parameters.add(Integer.toString(algoInfo.getMaxDegree()));
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

    private FgsContinuousRunInfo createDefaultFgsContinuousRunInfo() {
        FgsContinuousRunInfo runInfo = new FgsContinuousRunInfo();
        runInfo.setPenaltyDiscount(4.0);
        runInfo.setFaithfulnessAssumed(true);
        runInfo.setMaxDegree(100);
        runInfo.setNonZeroVarianceValidation(true);
        runInfo.setUniqueVarNameValidation(true);
        runInfo.setVerbose(true);
        runInfo.setJvmMaxMem(1);

        return runInfo;
    }

    private FgsDiscreteRunInfo createDefaultFgsDiscreteRunInfo() {
        FgsDiscreteRunInfo runInfo = new FgsDiscreteRunInfo();
        runInfo.setSamplePrior(1.0);
        runInfo.setStructurePrior(1.0);
        runInfo.setFaithfulnessAssumed(true);
        runInfo.setMaxDegree(100);
        runInfo.setUniqueVarNameValidation(true);
        runInfo.setLimitNumOfCategory(true);
        runInfo.setVerbose(true);
        runInfo.setJvmMaxMem(1);

        return runInfo;
    }

}
