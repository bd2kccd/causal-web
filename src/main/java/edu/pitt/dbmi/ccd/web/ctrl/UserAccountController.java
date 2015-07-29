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
import edu.pitt.dbmi.ccd.db.service.PersonService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewController.DIR_BROWSER;
import edu.pitt.dbmi.ccd.web.model.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * May 14, 2015 2:13:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class UserAccountController implements ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountController.class);

    private final UserAccountService userAccountService;

    private final PersonService personService;

    private final DefaultPasswordService passwordService;

    private final AppUserService appUserService;

    @Autowired(required = true)
    public UserAccountController(
            UserAccountService userAccountService,
            PersonService personService,
            DefaultPasswordService passwordService,
            AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.personService = personService;
        this.passwordService = passwordService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = SETUP, method = RequestMethod.GET)
    public String setupNewUser(
            @Value("${app.webapp:true}") final boolean isWebApplication,
            Model model) {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            if (isWebApplication) {
                return REDIRECT_LOGIN;
            } else {
                model.addAttribute("person", new Person());

                return SETUP;
            }
        }
    }

    @RequestMapping(value = SETUP, method = RequestMethod.POST)
    public String setupNewUserAccount(
            @Value("${app.default.pwd:password123}") String defaultPassword,
            @Value("${app.setup.error:Unable to setup initial settings.}") String setupErrMsg,
            @Value("${app.login.error:Unable to setup initial settings.}") String signInErrMsg,
            @ModelAttribute("person") Person person,
            Model model) {
        String baseDir = person.getWorkspace();
        Path workspace = Paths.get(baseDir);
        if (Files.exists(workspace)) {
            if (!Files.isDirectory(workspace)) {
                model.addAttribute("errorMsg", "Workspace provided is not a directory.");
                return SETUP;
            }
        } else {
            model.addAttribute("errorMsg", "Workspace directory does not exist.");
            return SETUP;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setActive(true);
        userAccount.setPassword(passwordService.encryptPassword(defaultPassword));
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
        userAccount.setUsername(System.getProperty("user.name"));
        userAccount.setPerson(person);
        try {
            userAccount = userAccountService.createNewUserAccount(userAccount);
        } catch (Exception exception) {
            LOGGER.warn(
                    String.format("Unable to set up new user account for %s.", userAccount.getUsername()),
                    exception);
            model.addAttribute("errorMsg", setupErrMsg);
            return SETUP;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), defaultPassword);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
        } catch (AuthenticationException exception) {
            LOGGER.warn(
                    String.format("Failed login attempt from user %s.", token.getUsername()),
                    exception);
            model.addAttribute("errorMsg", signInErrMsg);
            return SETUP;
        }

        model.addAttribute("appUser", appUserService.createAppUser(userAccount));

        return REDIRECT_HOME;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerNewUser(
            @Value("${app.server.workspace}") String workspace,
            final UserRegistration userRegistration,
            final RedirectAttributes redirectAttributes) {
        String username = userRegistration.getUsername();
        if (userAccountService.findByUsername(username) == null) {
            String email = userRegistration.getEmail();
            String password = userRegistration.getPassword();

            Person person = new Person();
            person.setFirstName("");
            person.setLastName("");
            person.setEmail(email);
            person.setWorkspace(Paths.get(workspace).toString());

            UserAccount userAccount = new UserAccount();
            userAccount.setActive(false);
            userAccount.setUsername(username);
            userAccount.setPassword(passwordService.encryptPassword(password));
            userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
            userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
            userAccount.setPerson(person);

            try {
                userAccountService.createNewUserAccount(userAccount);
            } catch (Exception exception) {
                LOGGER.warn(
                        String.format("Unable to register new user account for %s.", userAccount.getUsername()),
                        exception);
                redirectAttributes.addFlashAttribute("errorMsg", String.format("Unable to create account for '%s'.", username));
            }

            String msg = "Thank you for your request."
                    + "We will review your account and notify you when it is available.";
            redirectAttributes.addFlashAttribute("successMsg", msg);
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", String.format("Username '%s' is already taken.", username));
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = DIR_BROWSER, method = RequestMethod.GET)
    public String browsingServerSideDirectory(
            @RequestParam(value = "dir", required = false) String directory,
            Model model) {
        if (directory == null) {
            directory = System.getProperty("user.home");
        }
        Path path = Paths.get(directory);
        model.addAttribute("itemList", FileUtility.getDirListing(path.toAbsolutePath().toString()));
        model.addAttribute("currDir", path.toAbsolutePath().toString());

        return DIR_BROWSER;
    }

    @RequestMapping(value = DIR_BROWSER, method = RequestMethod.POST)
    public String createNewDirectory(
            @RequestParam(value = "dir", required = false) String directory,
            @RequestParam(value = "newFolder") String newFolder, Model model) {
        if (directory == null) {
            directory = System.getProperty("user.home");
        }
        Path path = Paths.get(directory);
        Path newDir = Paths.get(path.toAbsolutePath().toString(), newFolder);
        if (Files.notExists(newDir)) {
            try {
                Files.createDirectories(newDir);
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
            }
        }
        model.addAttribute("itemList", FileUtility.getDirListing(path.toAbsolutePath().toString()));
        model.addAttribute("currDir", path.toAbsolutePath().toString());

        return DIR_BROWSER;
    }

}
