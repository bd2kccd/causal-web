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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * May 14, 2015 12:39:47 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class ApplicationController implements ViewController {

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public ApplicationController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @RequestMapping(value = HOME, method = RequestMethod.GET)
    public String goHome(@ModelAttribute("appUser") AppUser appUser, Model model) {
        String userFullName = appUser.getFirstName() + " " + appUser.getLastName();

        model.addAttribute("userFullName", userFullName);
        model.addAttribute("lastLogin", FileUtility.DATE_FORMAT.format(appUser.getLastLoginDate()));

        return HOME;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logOut(Model model) {
        String url;

        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            model.addAttribute("successMsg", "You Have Successfully Logged Out.");
            url = LOGIN;
        } else {
            url = REDIRECT_LOGIN;
        }

        return url;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(
            @Value("${app.uploadDir:upload}") final String uploadDirectory,
            @Value("${app.outputDir:output}") final String outputDirectory,
            @Value("${app.libDir:lib}") String libDirectory,
            @Value("${app.tempDir:tmp}") final String tmpDirectory,
            final UsernamePasswordToken credentials,
            final Model model) {
        String url;

        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(credentials);
            url = REDIRECT_HOME;
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", "Invalid username and/or password.");
            url = LOGIN;
        }

        String username = (String) currentUser.getPrincipal();
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount != null) {
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

            AppUser appUser = new AppUser();
            appUser.setUsername(userAccount.getUsername());
            appUser.setFirstName(person.getFirstName());
            appUser.setLastName(person.getLastName());
            appUser.setLastLoginDate(userAccount.getLastLoginDate());
            appUser.setWebUser(false);
            appUser.setUploadDirectory(uploadDir.toString());
            appUser.setOutputDirectory(outputDir.toString());
            appUser.setLibDirectory(libDir.toString());
            appUser.setTmpDirectory(tmpDir.toString());
            model.addAttribute("appUser", appUser);
        }

        return url;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(
            @Value("${app.webapp:true}") final boolean isWebApplication,
            @Value("${app.default.pwd:password123}") final String defaultPassword,
            @Value("${app.login.error:Unable to setup initial settings.}") final String signInErrMsg,
            @Value("${app.uploadDir:upload}") final String uploadDirectory,
            @Value("${app.outputDir:output}") final String outputDirectory,
            @Value("${app.libDir:lib}") String libDirectory,
            @Value("${app.tempDir:tmp}") final String tmpDirectory,
            final Model model) {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            if (isWebApplication) {
                return LOGIN;
            } else {
                String username = System.getProperty("user.name");
                UserAccount userAccount = userAccountService.findByUsername(username);
                if (userAccount == null) {
                    return REDIRECT_SETUP;
                }

                UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), defaultPassword);
                token.setRememberMe(true);
                Subject currentUser = SecurityUtils.getSubject();
                try {
                    currentUser.login(token);
                } catch (AuthenticationException exception) {
                    model.addAttribute("errorMsg", signInErrMsg);
                    return REDIRECT_SETUP;
                }

                userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
                userAccountService.save(userAccount);

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

                AppUser appUser = new AppUser();
                appUser.setUsername(userAccount.getUsername());
                appUser.setFirstName(person.getFirstName());
                appUser.setLastName(person.getLastName());
                appUser.setLastLoginDate(userAccount.getLastLoginDate());
                appUser.setWebUser(false);
                appUser.setUploadDirectory(uploadDir.toString());
                appUser.setOutputDirectory(outputDir.toString());
                appUser.setLibDirectory(libDir.toString());
                appUser.setTmpDirectory(tmpDir.toString());
                model.addAttribute("appUser", appUser);

                return REDIRECT_HOME;
            }
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showIndexPage(final Model model) {
        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "/404", method = RequestMethod.GET)
    public String showPageNotFound() {
        return "404";
    }

}
