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

import edu.pitt.dbmi.ccd.db.service.AlgorithmRunLogService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.model.algo.CommonGFCIAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.GFCIcAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.GFCIdAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.GFCImCGAlgoOpt;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmRunService;
import edu.pitt.dbmi.ccd.web.util.TetradCmdOptions;
import static edu.pitt.dbmi.ccd.web.util.TetradCmdOptions.FAITHFULNESS_ASSUMED;
import static edu.pitt.dbmi.ccd.web.util.TetradCmdOptions.MAX_DEGREE;
import static edu.pitt.dbmi.ccd.web.util.TetradCmdOptions.SKIP_LATEST;
import java.util.HashMap;
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
 * May 28, 2017 9:50:44 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/gfci")
public class GFCIController extends AbstractTetradAlgoController implements ViewPath, TetradCmdOptions {

    private final String GFCIC_ALGO_NAME = "gfcic";
    private final String GFCID_ALGO_NAME = "gfcid";
    private final String GFCIM_ALGO_NAME = "gfcim";

    private final AlgorithmRunLogService algorithmRunLogService;
    private final AlgorithmRunService algorithmRunService;
    private final CcdProperties ccdProperties;

    @Autowired
    public GFCIController(AlgorithmRunLogService algorithmRunLogService, AlgorithmRunService algorithmRunService, CcdProperties ccdProperties, DataFileService dataFileService, AppUserService appUserService) {
        super(dataFileService, appUserService);
        this.algorithmRunLogService = algorithmRunLogService;
        this.algorithmRunService = algorithmRunService;
        this.ccdProperties = ccdProperties;
    }

    @RequestMapping(value = "gfcim", method = RequestMethod.POST)
    public String runGFCImCG(
            @ModelAttribute("algoOpt") final GFCImCGAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCIm-CG", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfci());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForMixed(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getGFCImParams(algoOpt), getFileSummary(algoOpt, appUser), GFCIM_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "gfcim", method = RequestMethod.GET)
    public String showGFCImCGView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserMixedDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GFCImCGAlgoOpt algoOpt = new GFCImCGAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return GFCI_MIXED_VIEW;
    }

    @RequestMapping(value = "gfcid", method = RequestMethod.POST)
    public String runGfciDiscrete(
            @ModelAttribute("algoOpt") final GFCIdAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCId", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfci());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForDiscrete(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getGFCIdParams(algoOpt), getFileSummary(algoOpt, appUser), GFCID_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "gfcid", method = RequestMethod.GET)
    public String showGfciDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserDiscreteDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GFCIdAlgoOpt algoOpt = new GFCIdAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return GFCI_DISC_VIEW;
    }

    @RequestMapping(value = "gfcic", method = RequestMethod.POST)
    public String runGfciContinuous(
            @ModelAttribute("algoOpt") final GFCIcAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("GFCIc", ccdProperties.getAlgoJar(), ccdProperties.getAlgoGfci());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForContinuous(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getGFCIcParams(algoOpt), getFileSummary(algoOpt, appUser), GFCIC_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "gfcic", method = RequestMethod.GET)
    public String showGfciContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserContinuousDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        GFCIcAlgoOpt algoOpt = new GFCIcAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return GFCI_CONT_VIEW;
    }

    private Map<String, String> getGFCImParams(GFCImCGAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(ALPHA.replaceAll("--", ""), Double.toString(algoOpt.getAlpha()));
        params.put(STRUCTURE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getStructurePrior()));
        params.put(DISCRETIZE.replaceAll("--", ""), algoOpt.isDiscretize() ? "true" : "false");
        params.put(NUM_CATEGORIES.replaceAll("--", ""), Integer.toString(algoOpt.getNumCategories()));

        getCommonGFCIParams(params, algoOpt);

        return params;
    }

    private Map<String, String> getGFCIdParams(GFCIdAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(ALPHA.replaceAll("--", ""), Double.toString(algoOpt.getAlpha()));
        params.put(SAMPLE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getSamplePrior()));
        params.put(STRUCTURE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getStructurePrior()));

        getCommonGFCIParams(params, algoOpt);

        return params;
    }

    private Map<String, String> getGFCIcParams(GFCIcAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(ALPHA.replaceAll("--", ""), Double.toString(algoOpt.getAlpha()));
        params.put(PENALTY_DISCOUNT.replaceAll("--", ""), Double.toString(algoOpt.getPenaltyDiscount()));

        getCommonGFCIParams(params, algoOpt);

        return params;
    }

    private void getCommonGFCIParams(Map<String, String> params, CommonGFCIAlgoOpt algoOpt) {
        params.put(MAX_DEGREE.replaceAll("--", ""), Integer.toString(algoOpt.getMaxDegree()));
        params.put(MAX_PATH_LENGTH.replaceAll("--", ""), Integer.toString(algoOpt.getMaxPathLength()));
        params.put(FAITHFULNESS_ASSUMED.replaceAll("--", ""), algoOpt.isFaithfulnessAssumed() ? "true" : "false");
        params.put(COMPLETE_RULE_SET_USED.replaceAll("--", ""), algoOpt.isCompleteRuleSetUsed() ? "true" : "false");
        params.put(BOOTSTRAP_ENSEMBLE.replaceAll("--", ""), Integer.toString(algoOpt.getBootstrapEnsemble()));
        params.put(BOOTSTRAP_SAMPLE_SIZE.replaceAll("--", ""), Integer.toString(algoOpt.getBootstrapSampleSize()));
    }

    private List<String> getParametersForMixed(GFCImCGAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        parameters.add(DELIMITER);
        parameters.add(algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username));
        parameters.add(DATATYPE);
        parameters.add("mixed");
        parameters.add(INDEPENDENCE_TEST);
        parameters.add(ccdProperties.getTestMixed());
        parameters.add(SCORE);
        parameters.add(ccdProperties.getScoreMixed());
        parameters.add(NUM_CATEGORIES);
        parameters.add(Integer.toString(algoOpt.getNumCategories()));

        // tetrad parameters
        parameters.add(ALPHA);
        parameters.add(Double.toString(algoOpt.getAlpha()));
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        if (algoOpt.isDiscretize()) {
            parameters.add(DISCRETIZE);
        }

        // get common parameters
        getCommonGFCIAlgoOpt(algoOpt, parameters);
        getBootstrapParameters(algoOpt, parameters);

        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }

        return parameters;
    }

    private List<String> getParametersForDiscrete(GFCIdAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        parameters.add(DELIMITER);
        parameters.add(algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username));
        parameters.add(DATATYPE);
        parameters.add("discrete");
        parameters.add(INDEPENDENCE_TEST);
        parameters.add(ccdProperties.getTestDiscrete());
        parameters.add(SCORE);
        parameters.add(ccdProperties.getScoreDiscrete());

        // tetrad parameters
        parameters.add(ALPHA);
        parameters.add(Double.toString(algoOpt.getAlpha()));
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(SAMPLE_PRIOR);
        parameters.add(Double.toString(algoOpt.getSamplePrior()));

        // get common parameters
        getCommonGFCIAlgoOpt(algoOpt, parameters);
        getBootstrapParameters(algoOpt, parameters);

        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipValidation()) {
            parameters.add(SKIP_VALIDATION);
        }

        return parameters;
    }

    private List<String> getParametersForContinuous(GFCIcAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        parameters.add(DELIMITER);
        parameters.add(algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username));
        parameters.add(DATATYPE);
        parameters.add("continuous");
        parameters.add(INDEPENDENCE_TEST);
        parameters.add(ccdProperties.getTestContinuous());
        parameters.add(SCORE);
        parameters.add(ccdProperties.getScoreContinuous());

        // tetrad parameters
        parameters.add(ALPHA);
        parameters.add(Double.toString(algoOpt.getAlpha()));
        parameters.add(PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoOpt.getPenaltyDiscount()));

        // get common parameters
        getCommonGFCIAlgoOpt(algoOpt, parameters);
        getBootstrapParameters(algoOpt, parameters);

        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipValidation()) {
            parameters.add(SKIP_VALIDATION);
        }

        return parameters;
    }

    private void getCommonGFCIAlgoOpt(CommonGFCIAlgoOpt commonGFCIAlgoOpt, List<String> parameters) {
        // common tetrad parameters
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(commonGFCIAlgoOpt.getMaxDegree()));
        parameters.add(MAX_PATH_LENGTH);
        parameters.add(Integer.toString(commonGFCIAlgoOpt.getMaxPathLength()));
        if (commonGFCIAlgoOpt.isCompleteRuleSetUsed()) {
            parameters.add(COMPLETE_RULE_SET_USED);
        }
        if (commonGFCIAlgoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }

        // server options
        parameters.add(SKIP_LATEST);
    }

}
