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
package edu.pitt.dbmi.ccd.web.ctrl.user;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.UserActivationException;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.UserService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
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
 * Aug 4, 2015 12:36:43 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "user/registration")
public class UserRegistrationController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationController.class);

    private final UserAccountService userAccountService;

    private final UserService userService;

    private final DefaultPasswordService passwordService;

    private final AppUserService appUserService;

    private final boolean webapp;

    @Autowired(required = true)
    public UserRegistrationController(UserAccountService userAccountService,
            UserService userService, DefaultPasswordService passwordService,
            AppUserService appUserService, boolean webapp) {
        this.userAccountService = userAccountService;
        this.userService = userService;
        this.passwordService = passwordService;
        this.appUserService = appUserService;
        this.webapp = webapp;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerWebUser(
            @Value("${ccd.server.workspace}") String workspace,
            final UserRegistration userRegistration,
            final RedirectAttributes redirectAttributes,
            final HttpServletRequest request) {
        String username = userRegistration.getUsername();
        if (userAccountService.findByUsername(username) == null) {
            try {
                userService.registerNewUser(userRegistration, workspace, request.getRequestURL().toString());
                String msg = "Thank you for registering."
                        + "Check your email to verify and activate your account.";
                redirectAttributes.addFlashAttribute("successMsg", msg);
            } catch (Exception exception) {
                LOGGER.warn(
                        String.format("Unable to register new user account for %s.", username),
                        exception);
                redirectAttributes.addFlashAttribute("errorMsg", String.format("Unable to create account for '%s'.", username));
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", String.format("Username '%s' is already taken.", username));
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String activateWebUser(
            @RequestParam(value = "user", required = true) final String user,
            @RequestParam(value = "key", required = true) final String activationKey,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsernameAndActivationKey(user, activationKey);
        if (userAccount == null) {
            throw new UserActivationException();
        } else {
            userAccount.setActive(Boolean.TRUE);
            userAccount.setActivationKey(null);
            userAccountService.saveUserAccount(userAccount);

            model.addAttribute("username", userAccount.getUsername());

            return "user/userActivationSuccess";
        }
    }

    @RequestMapping(value = SETUP, method = RequestMethod.GET)
    public String desktopUserRegistration(final Model model) {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            if (webapp) {
                return REDIRECT_LOGIN;
            } else {
                model.addAttribute("person", new Person());

                return SETUP_VIEW;
            }
        }
    }

    @RequestMapping(value = SETUP, method = RequestMethod.POST)
    public String registerDesktopUser(
            @Value("${ccd.desktop.default.pwd:password123}") final String defaultPassword,
            @Value("${ccd.desktop.set.error:Unable to setup initial settings.}") final String setupErrMsg,
            @Value("${ccd.desktop.login.error:Unable to sign in desktop user.}") final String signInErrMsg,
            @ModelAttribute("person") final Person person,
            final Model model) {
        String baseDir = person.getWorkspace();
        Path workspace = Paths.get(baseDir);
        if (Files.exists(workspace)) {
            if (!Files.isDirectory(workspace)) {
                model.addAttribute("errorMsg", "Workspace provided is not a directory.");
                return SETUP_VIEW;
            }
        } else {
            model.addAttribute("errorMsg", "Workspace directory does not exist.");
            return SETUP_VIEW;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setActive(true);
        userAccount.setPassword(passwordService.encryptPassword(defaultPassword));
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
        userAccount.setUsername(System.getProperty("user.name"));
        userAccount.setPerson(person);

        try {
            userAccount = userAccountService.saveUserAccount(userAccount);
        } catch (Exception exception) {
            LOGGER.warn(
                    String.format("Unable to set up new user account for %s.", userAccount.getUsername()),
                    exception);
            model.addAttribute("errorMsg", setupErrMsg);
            return SETUP_VIEW;
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
            return SETUP_VIEW;
        }

        model.addAttribute("appUser", appUserService.createAppUser(userAccount));

        return REDIRECT_HOME;
    }

}
