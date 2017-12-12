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
import edu.pitt.dbmi.ccd.web.model.algo.CommonFGESAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.FGEScAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.FGESdAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.FGESmAlgoOpt;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmRunService;
import edu.pitt.dbmi.ccd.web.util.TetradCmdOptions;
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
 * Nov 17, 2015 11:42:14 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/fges")
public class FGESController extends AbstractTetradAlgoController implements ViewPath, TetradCmdOptions {

    private final String FGESC_ALGO_NAME = "fgesc";
    private final String FGESD_ALGO_NAME = "fgesd";
    private final String FGESM_ALGO_NAME = "fgesm";

    private final AlgorithmRunLogService algorithmRunLogService;
    private final AlgorithmRunService algorithmRunService;
    private final CcdProperties ccdProperties;

    @Autowired
    public FGESController(AlgorithmRunLogService algorithmRunLogService, AlgorithmRunService algorithmRunService, CcdProperties ccdProperties, DataFileService dataFileService, AppUserService appUserService) {
        super(dataFileService, appUserService);
        this.algorithmRunLogService = algorithmRunLogService;
        this.algorithmRunService = algorithmRunService;
        this.ccdProperties = ccdProperties;
    }

    @RequestMapping(value = "fgesm", method = RequestMethod.POST)
    public String runshowFgesmCG(
            @ModelAttribute("algoOpt") final FGESmAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESm", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFges());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForMixed(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getFGESmParams(algoOpt), getFileSummary(algoOpt, appUser), FGESM_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "fgesm", method = RequestMethod.GET)
    public String showFgesmCGView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserMixedDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FGESmAlgoOpt algoOpt = new FGESmAlgoOpt();

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

        return FGES_MIXED_VIEW;
    }

    @RequestMapping(value = "fgesd", method = RequestMethod.POST)
    public String runFgesDiscrete(
            @ModelAttribute("algoOpt") final FGESdAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESd", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFges());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForDiscrete(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getFGESdParams(algoOpt), getFileSummary(algoOpt, appUser), FGESD_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "fgesd", method = RequestMethod.GET)
    public String showFgesDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserDiscreteDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FGESdAlgoOpt algoOpt = new FGESdAlgoOpt();

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

        return FGES_DISC_VIEW;
    }

    @RequestMapping(value = "fgesc", method = RequestMethod.POST)
    public String runFgesContinuous(
            @ModelAttribute("algoOpt") final FGEScAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESc", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFges());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForContinuous(algoOpt, appUser.getUsername()));

        algorithmRunService.addToQueue(jobRequest, appUser.getUsername());
        algorithmRunLogService.logAlgorithmRun(getFGEScParams(algoOpt), getFileSummary(algoOpt, appUser), FGESC_ALGO_NAME, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "fgesc", method = RequestMethod.GET)
    public String showFgesContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmRunService.getUserContinuousDataset(appUser.getUsername());
        Map<String, String> prior = algorithmRunService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FGEScAlgoOpt algoOpt = new FGEScAlgoOpt();

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

        return FGES_CONT_VIEW;
    }

    private Map<String, String> getFGESmParams(FGESmAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(STRUCTURE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getStructurePrior()));
        params.put(DISCRETIZE.replaceAll("--", ""), algoOpt.isDiscretize() ? "true" : "false");
        params.put(NUM_CATEGORIES.replaceAll("--", ""), Integer.toString(algoOpt.getNumCategories()));

        getCommonFGESParams(params, algoOpt);

        return params;
    }

    private Map<String, String> getFGESdParams(FGESdAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(SAMPLE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getSamplePrior()));
        params.put(STRUCTURE_PRIOR.replaceAll("--", ""), Double.toString(algoOpt.getStructurePrior()));

        getCommonFGESParams(params, algoOpt);

        return params;
    }

    private Map<String, String> getFGEScParams(FGEScAlgoOpt algoOpt) {
        Map<String, String> params = new HashMap<>();
        params.put(PENALTY_DISCOUNT.replaceAll("--", ""), Double.toString(algoOpt.getPenaltyDiscount()));

        getCommonFGESParams(params, algoOpt);

        return params;
    }

    private void getCommonFGESParams(Map<String, String> params, CommonFGESAlgoOpt algoOpt) {
        params.put(MAX_DEGREE.replaceAll("--", ""), Integer.toString(algoOpt.getMaxDegree()));
        params.put(FAITHFULNESS_ASSUMED.replaceAll("--", ""), algoOpt.isFaithfulnessAssumed() ? "true" : "false");
        params.put(SYMMETRIC_FIRST_STEP.replaceAll("--", ""), algoOpt.isSymmetricFirstStep() ? "true" : "false");
    }

    private List<String> getParametersForMixed(FGESmAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        parameters.add(DELIMITER);
        parameters.add(algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username));
        parameters.add(DATATYPE);
        parameters.add("mixed");
        parameters.add(SCORE);
        parameters.add(ccdProperties.getScoreMixed());
        parameters.add(NUM_CATEGORIES);
        parameters.add(Integer.toString(algoOpt.getNumCategories()));

        // tetrad parameters
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        if (algoOpt.isDiscretize()) {
            parameters.add(DISCRETIZE);
        }

        // get common parameters
        getCommonFGESAlgoOpt(algoOpt, parameters);

        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }

        return parameters;
    }

    private List<String> getParametersForDiscrete(FGESdAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        parameters.add(DELIMITER);
        parameters.add(algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username));
        parameters.add(DATATYPE);
        parameters.add("discrete");
        parameters.add(SCORE);
        parameters.add(ccdProperties.getScoreDiscrete());

        // tetrad parameters
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(SAMPLE_PRIOR);
        parameters.add(Double.toString(algoOpt.getSamplePrior()));

        // get common parameters
        getCommonFGESAlgoOpt(algoOpt, parameters);

        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipValidation()) {
            parameters.add(SKIP_VALIDATION);
        }

        return parameters;
    }

    private List<String> getParametersForContinuous(FGEScAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        parameters.add(DELIMITER);
        parameters.add(algorithmRunService.getFileDelimiter(algoOpt.getDataset(), username));
        parameters.add(DATATYPE);
        parameters.add("continuous");
        parameters.add(SCORE);
        parameters.add(ccdProperties.getScoreContinuous());

        // tetrad parameters
        parameters.add(PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoOpt.getPenaltyDiscount()));

        // get common parameters
        getCommonFGESAlgoOpt(algoOpt, parameters);

        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipValidation()) {
            parameters.add(SKIP_VALIDATION);
        }

        return parameters;
    }

    private void getCommonFGESAlgoOpt(CommonFGESAlgoOpt commonFGESAlgoOpt, List<String> parameters) {
        // common tetrad parameters
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(commonFGESAlgoOpt.getMaxDegree()));
        if (commonFGESAlgoOpt.isSymmetricFirstStep()) {
            parameters.add(SYMMETRIC_FIRST_STEP);
        }
        if (commonFGESAlgoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }

        // server options
        parameters.add(SKIP_LATEST);
    }

}
