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

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.DataFileInfoService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.JobQueueInfoService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.GesRunInfo;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
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
public class GesController extends AlgorithmController implements ViewController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GesController.class);

	private final String ges;

	//private final AlgorithmService algorithmService;

	private final JobQueueInfoService jobQueueInfoService;
	
	private final UserAccountService userAccountService;

	@Autowired(required = true)
	public GesController(@Value("${app.gesApp:edu.pitt.dbmi.ccd.algorithm.tetrad.GesApp}") String ges,
			//AlgorithmService algorithmService,
			@Value("${app.algoJar:ccd-algorithm-1.0-SNAPSHOT.jar}") String algorithmJar,
			VariableTypeService variableTypeService, FileDelimiterService fileDelimiterService,
			DataFileService dataFileService, DataFileInfoService dataFileInfoService, DataService dataService,
			JobQueueInfoService queuedJobInfoService,
			UserAccountService userAccountService) {
		super(algorithmJar, variableTypeService, fileDelimiterService, dataFileService, dataFileInfoService,
				dataService);
		this.ges = ges;
		//this.algorithmService = algorithmService;
		this.jobQueueInfoService = queuedJobInfoService;
		this.userAccountService = userAccountService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showGesView(Model model, @ModelAttribute("appUser") AppUser appUser) {
		GesRunInfo info = new GesRunInfo();
		info.setPenaltyDiscount(2.0);
		info.setDepth(3);
		info.setVerbose(Boolean.TRUE);
		info.setJvmOptions("");

		model.addAttribute("gesRunInfo", info);

		model.addAttribute("datasetList", directoryFileListing(appUser.getUsername(), appUser.getUploadDirectory()));

		return GES;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String runGes(Model model, @ModelAttribute("gesRunInfo") GesRunInfo info,
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
		commands.add(ges);

		Path dataset = Paths.get(appUser.getUploadDirectory(), info.getDataset());
		commands.add("--data");
		commands.add(dataset.toString());

		commands.add("--delimiter");
		commands.add(getFileDelimiter(info.getDataset(), appUser.getUploadDirectory()));

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

		String command = StringUtils.join(commands.toArray(), ";");
		System.out.println(command);
		System.out.println(fileName);
		Optional<UserAccount> userAccount = userAccountService.findByUsername(appUser.getUsername());
		JobQueueInfo queuedJobInfo = new JobQueueInfo(null, "GES", command, fileName, appUser.getTmpDirectory(),
				appUser.getOutputDirectory(), new Integer(0), new Date(System.currentTimeMillis()),Collections.singleton(userAccount.get()));
		queuedJobInfo = jobQueueInfoService.saveJobIntoQueue(queuedJobInfo);
        LOGGER.info("Add Job into Queue: " + queuedJobInfo.getId());

		/*try {
			algorithmService.runAlgorithm(commands, fileName, appUser.getTmpDirectory(), appUser.getOutputDirectory());
		} catch (Exception exception) {
			LOGGER.error("Unable to run GES.", exception);
		}*/

		model.addAttribute("title", "GES is Added into Queue");

		return REDIRECT_JOB_QUEUE;
	}

}
