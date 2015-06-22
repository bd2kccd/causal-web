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

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 19, 2015 9:40:09 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AppUserService {

    private final String uploadDirectory;

    private final String outputDirectory;

    private final String libDirectory;

    private final String tmpDirectory;

    private final boolean webUser;

    @Autowired(required = true)
    public AppUserService(
            @Value("${app.uploadDir:upload}") String uploadDirectory,
            @Value("${app.outputDir:output}") String outputDirectory,
            @Value("${app.libDir:lib}") String libDirectory,
            @Value("${app.tempDir:tmp}") String tmpDirectory,
            @Value("${app.webapp:false}") boolean webUser) {
        this.uploadDirectory = uploadDirectory;
        this.outputDirectory = outputDirectory;
        this.libDirectory = libDirectory;
        this.tmpDirectory = tmpDirectory;
        this.webUser = webUser;
    }

    public AppUser createAppUser(UserAccount userAccount) {
        AppUser appUser = new AppUser();

        Person person = userAccount.getPerson();
        String baseDir = person.getWorkspaceDirectory();
        Path uploadDir = Paths.get(baseDir, uploadDirectory);
        Path outputDir = Paths.get(baseDir, outputDirectory);
        Path libDir = Paths.get(baseDir, libDirectory);
        Path tmpDir = Paths.get(baseDir, tmpDirectory);
        Path[] directories = {uploadDir, outputDir, tmpDir, libDir};
        for (Path directory : directories) {
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    exception.printStackTrace(System.err);
                }
            }
        }

        Resource resource = new ClassPathResource("/lib");
        try {
            Path libPath = Paths.get(resource.getFile().getAbsolutePath());
            Files.walk(libPath).filter(Files::isRegularFile).forEach(file -> {
                Path destFile = Paths.get(libDir.toAbsolutePath().toString(), libPath.relativize(file).toString());
                if (!Files.exists(destFile)) {
                    try {
                        Files.copy(file, destFile);
                    } catch (IOException exception) {
                        exception.printStackTrace(System.err);
                    }
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        appUser.setUsername(userAccount.getUsername());
        appUser.setFirstName(person.getFirstName());
        appUser.setLastName(person.getLastName());
        appUser.setLastLoginDate(userAccount.getLastLoginDate());
        appUser.setWebUser(webUser);
        appUser.setUploadDirectory(uploadDir.toString());
        appUser.setOutputDirectory(outputDir.toString());
        appUser.setLibDirectory(libDir.toString());
        appUser.setTmpDirectory(tmpDir.toString());

        return appUser;
    }

}
