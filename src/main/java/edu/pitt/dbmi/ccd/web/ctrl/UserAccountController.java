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
import edu.pitt.dbmi.ccd.web.service.PersonService;
import edu.pitt.dbmi.ccd.web.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.util.AppUserFactory;
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
 * May 14, 2015 2:13:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class UserAccountController implements ViewController {

    private final String defaultPassword;

    private final String setupErrMsg;

    private final String signInErrMsg;

    protected final String uploadDir;

    protected final String outputDir;

    protected final String tmpDir;

    private final UserAccountService userAccountService;

    private final PersonService personService;

    private final DefaultPasswordService passwordService;

    @Autowired(required = true)
    public UserAccountController(
            @Value("${app.default.pwd:password123}") String defaultPassword,
            @Value("${app.setup.error:Unable to setup initial settings.}") String setupErrMsg,
            @Value("${app.login.error:Unable to setup initial settings.}") String signInErrMsg,
            @Value("${app.uploadDir:upload}") String uploadDir,
            @Value("${app.outputDir:output}") String outputDir,
            @Value("${app.tempDir:tmp}") String tmpDir,
            UserAccountService userAccountService,
            PersonService personService,
            DefaultPasswordService passwordService) {
        this.defaultPassword = defaultPassword;
        this.setupErrMsg = setupErrMsg;
        this.signInErrMsg = signInErrMsg;
        this.uploadDir = uploadDir;
        this.outputDir = outputDir;
        this.tmpDir = tmpDir;
        this.userAccountService = userAccountService;
        this.personService = personService;
        this.passwordService = passwordService;
    }

    @RequestMapping(value = SETUP, method = RequestMethod.POST)
    public String setupNewUserAccount(@ModelAttribute("person") Person person, Model model) {
        UserAccount userAccount = new UserAccount();
        userAccount.setActive(true);
        userAccount.setPassword(passwordService.encryptPassword(defaultPassword));
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setUsername(System.getProperty("user.name"));
        userAccount.setPerson(person);

        Path workspace = Paths.get(person.getWorkspaceDirectory());
        if (Files.exists(workspace)) {
            if (!Files.isDirectory(workspace)) {
                model.addAttribute("errorMsg", "Workspace provided is not a directory.");
                return SETUP;
            }
        } else {
            model.addAttribute("errorMsg", "Workspace directory does not exist.");
            return SETUP;
        }

        try {
            userAccount = userAccountService.createNewUserAccount(userAccount);
        } catch (Exception exception) {
            model.addAttribute("errorMsg", setupErrMsg);
            return SETUP;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), defaultPassword);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", signInErrMsg);
            return SETUP;
        }

//        model.addAttribute("appUser", AppUserFactory.createAppUser(userAccount, false));
        // creating user local directory.  Still need to handle errors properly.
        String baseDir = person.getWorkspaceDirectory();
        Path[] directories = {
            Paths.get(baseDir, uploadDir),
            Paths.get(baseDir, outputDir),
            Paths.get(baseDir, tmpDir)
        };
        for (Path directory : directories) {
            if (Files.notExists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException exception) {
                    exception.printStackTrace(System.err);
                }
            }
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerNewUserAccount(final UserAccount userAccount, Model model) {
        String username = userAccount.getUsername();
        if (userAccountService.findByUsername(username) != null) {
            model.addAttribute("errorMsg", String.format("Username '%s' is already taken.", username));
            return LOGIN;
        }

        String pwd = userAccount.getPassword();
        userAccount.setPassword(passwordService.encryptPassword(pwd));
        userAccount.setActive(true);
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setPerson(new Person("Default", "User", "user@localhost", ""));
        try {
            userAccountService.createNewUserAccount(userAccount);
        } catch (Exception exception) {
            model.addAttribute("errorMsg", String.format("Unable to create account for '%s'.", username));
            return LOGIN;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(userAccount.getUsername(), pwd);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", "Unable to sign in.");
            return LOGIN;
        }

        model.addAttribute("appUser", AppUserFactory.createAppUser(userAccount, false));

        return REDIRECT_HOME;
    }

    @RequestMapping(value = USER_PROFILE, method = RequestMethod.GET)
    public String showPageUserProfile(@ModelAttribute("appUser") AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        model.addAttribute("person", userAccount.getPerson());

        return USER_PROFILE;
    }

    @RequestMapping(value = USER_PROFILE, method = RequestMethod.POST)
    public String saveUserProfile(
            @ModelAttribute("person") Person person,
            @ModelAttribute("appUser") AppUser appUser,
            Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        userAccount.setPerson(person);
        userAccountService.save(userAccount);

        model.addAttribute("appUser", AppUserFactory.createAppUser(userAccount, false));

        return REDIRECT_USER_PROFILE;
    }

}
