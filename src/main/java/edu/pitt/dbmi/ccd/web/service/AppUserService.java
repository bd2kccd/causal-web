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
import edu.pitt.dbmi.ccd.web.model.AppUser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
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

    final String workspace;

    final String dataFolder;
    final String resultFolder;
    final String libFolder;
    final String tmpFolder;

    final String algorithmResultFolder;
    final String compareResultFolder;

    @Autowired
    public AppUserService(
            @Value("${ccd.server.workspace}") String workspace,
            @Value("${ccd.folder.data:data}") String dataFolder,
            @Value("${ccd.folder.results:results}") String resultFolder,
            @Value("${ccd.folder.lib:lib}") String libFolder,
            @Value("${ccd.folder.tmp:tmp}") String tmpFolder,
            @Value("${ccd.folder.results.algorithm:algorithm}") String algorithmResultFolder,
            @Value("${ccd.folder.results.comparison:comparison}") String compareResultFolder) {
        this.workspace = workspace;
        this.dataFolder = dataFolder;
        this.resultFolder = resultFolder;
        this.libFolder = libFolder;
        this.tmpFolder = tmpFolder;
        this.algorithmResultFolder = algorithmResultFolder;
        this.compareResultFolder = compareResultFolder;
    }

    public AppUser createAppUser(final UserAccount userAccount) {
        String username = userAccount.getUsername();
        Path[] directories = {
            Paths.get(workspace, username, dataFolder),
            Paths.get(workspace, username, resultFolder),
            Paths.get(workspace, libFolder),
            Paths.get(workspace, username, tmpFolder),
            Paths.get(workspace, username, resultFolder, algorithmResultFolder),
            Paths.get(workspace, username, resultFolder, compareResultFolder)
        };
        for (Path directory : directories) {
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to create directory '%s'.", directory), exception);
                }
            }
        }

        Person person = userAccount.getPerson();
        String firstName = person.getFirstName();
        String middleName = person.getMiddleName();
        String lastName = person.getLastName();
        Date lastLoginDate = userAccount.getLastLoginDate();

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setFirstName(firstName == null ? "" : firstName);
        appUser.setMiddleName(middleName == null ? "" : middleName);
        appUser.setLastName(lastName == null ? "" : lastName);
        appUser.setLastLogin(lastLoginDate == null ? "" : FilePrint.fileTimestamp(lastLoginDate.getTime()));

        return appUser;
    }
}
