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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.cloud.CloudService;
import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 5, 2015 5:51:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AppUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserService.class);

    private final Boolean webapp;

    final String dataFolder;

    final String resultFolder;

    final String libFolder;

    final String tmpFolder;
    
    private final CloudService cloudService;

    @Autowired(required = true)
    public AppUserService(
            Boolean webapp,
            @Value("${ccd.data.folder:data}") String dataFolder,
            @Value("${ccd.result.folder:result}") String resultFolder,
            @Value("${ccd.lib.folder:lib}") String libFolder,
            @Value("${ccd.tmp.folder:tmp}") String tmpFolder,
            CloudService cloudService) {
        this.webapp = webapp;
        this.dataFolder = dataFolder;
        this.resultFolder = resultFolder;
        this.libFolder = libFolder;
        this.tmpFolder = tmpFolder;
        this.cloudService = cloudService;
    }

    public AppUser createAppUser(final UserAccount userAccount) {
        Person person = userAccount.getPerson();
        String workspace = person.getWorkspace();
        String username = userAccount.getUsername();

        Path dataDir = Paths.get(workspace, username, dataFolder);
        Path resultDir = Paths.get(workspace, username, resultFolder);
        Path libDir = Paths.get(workspace, libFolder);
        Path tmpDir = Paths.get(workspace, username, tmpFolder);

        // create user directories
        Path[] directories = {dataDir, resultDir, tmpDir, libDir};
        for (Path directory : directories) {
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to create directory '%s'.", directory), exception);
                }
            }
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setFirstName(person.getFirstName());
        appUser.setLastName(person.getLastName());
        appUser.setLastLogin(FileUtility.DATE_FORMAT.format(userAccount.getLastLoginDate()));
        appUser.setWebUser(webapp);
        appUser.setDataDirectory(dataDir.toString());
        appUser.setLibDirectory(libDir.toString());
        appUser.setResultDirectory(resultDir.toString());
        appUser.setTmpDirectory(tmpDir.toString());
        
        if(!webapp){
        	appUser.setWebServiceOnline(cloudService.isWebServiceOnline());
        	appUser.setLastTimeWebServiceMonitored(FilePrint.fileTimestamp(System.currentTimeMillis()));
        }

        return appUser;
    }

}
