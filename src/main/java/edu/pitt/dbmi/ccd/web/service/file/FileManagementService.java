/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.service.file;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 5, 2016 12:11:29 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagementService.class);

    private final CcdProperties ccdProperties;

    @Autowired
    public FileManagementService(CcdProperties ccdProperties) {
        this.ccdProperties = ccdProperties;
    }

    public void createUserDirectories(UserAccount userAccount) {
        String workspace = ccdProperties.getWorkspaceDir();
        String username = userAccount.getUsername();
        String resultFolder = ccdProperties.getResultFolder();
        Path[] directories = {
            Paths.get(workspace, username, ccdProperties.getDataFolder()),
            Paths.get(workspace, username, resultFolder),
            Paths.get(workspace, ccdProperties.getLibFolder()),
            Paths.get(workspace, username, ccdProperties.getTmpFolder()),
            Paths.get(workspace, username, resultFolder, ccdProperties.getResultAlgorithmFolder()),
            Paths.get(workspace, username, resultFolder, ccdProperties.getResultComparisonFolder())
        };
        for (Path directory : directories) {
            System.out.println(directory.toAbsolutePath().toString());
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to create directory '%s'.", directory), exception);
                }
            }
        }
    }

}
