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
import edu.pitt.dbmi.ccd.web.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.util.ApplicationUtility;

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
import org.springframework.web.context.request.WebRequest;

/**
 *
 * May 14, 2015 2:13:27 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
public class UserAccountController implements ViewController {

    private final boolean isWebApplication;

    private final String defaultPassword;

    private final String setupErrMsg;

    private final String signInErrMsg;

    private final UserAccountService userAccountService;

    private final DefaultPasswordService passwordService;

    @Autowired(required = true)
    public UserAccountController(
    		@Value("${app.webapp:true}") boolean isWebApplication,
            @Value("${app.default.pwd:password123}") String defaultPassword,
            @Value("${app.setup.error:Unable to setup initial settings.}") String setupErrMsg,
            @Value("${app.login.error:Unable to setup initial settings.}") String signInErrMsg,
            UserAccountService userAccountService,
            DefaultPasswordService passwordService) {
    	this.isWebApplication = isWebApplication;
        this.defaultPassword = defaultPassword;
        this.setupErrMsg = setupErrMsg;
        this.signInErrMsg = signInErrMsg;
        this.userAccountService = userAccountService;
        this.passwordService = passwordService;
    }

    @RequestMapping(value = SETUP, method = RequestMethod.POST)
    public String setupNewUserAccount(
    		@ModelAttribute("person") Person person, 
    		Model model) {
    	final String username = System.getProperty("user.name");
        UserAccount userAccount = new UserAccount();
        userAccount.setActive(true);
        userAccount.setPassword(passwordService.encryptPassword(defaultPassword));
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
        userAccount.setUsername(username);
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
            userAccount = userAccountService.saveUserAccount(userAccount);
        } catch (Exception exception) {
            model.addAttribute("errorMsg", setupErrMsg);
            return SETUP;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(
        		userAccount.getUsername(), defaultPassword);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", signInErrMsg);
            return SETUP;
        }

        ApplicationUtility.addAppUser2Model(isWebApplication, userAccount, model);

        return REDIRECT_HOME;
    }

    @RequestMapping(value = REGISTRATION, method = RequestMethod.POST)
    public String registerNewUserAccount(final UserAccount userAccount, Model model) {
        final String username = userAccount.getUsername();
        if (userAccountService.findByUsername(username) != null) {
            model.addAttribute("errorMsg", 
            		String.format("Username '%s' is already taken.", username));
            return LOGIN;
        }

        String pwd = userAccount.getPassword();
        userAccount.setPassword(passwordService.encryptPassword(pwd));
        userAccount.setActive(true);
        userAccount.setCreatedDate(new Date(System.currentTimeMillis()));
        userAccount.setPerson(new Person("Default", "User", "user@localhost", ""));
        try {
            userAccountService.saveUserAccount(userAccount);
        } catch (Exception exception) {
            model.addAttribute("errorMsg", 
            		String.format("Unable to create account for '%s'.", username));
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

        ApplicationUtility.addAppUser2Model(isWebApplication, userAccount, model);

        return REDIRECT_HOME;
    }

    @RequestMapping(value = USERPROFILE, method = RequestMethod.GET)
    public String showPageUserProfile(Model model, WebRequest request) {
    	return ApplicationUtility.forwardBasedOnSessionExisting(
    			isWebApplication, defaultPassword, signInErrMsg, userAccountService, 
    			model, USERPROFILE);
    }
    
    @RequestMapping(value = USERPROFILE, method = RequestMethod.POST)
    public String saveUserProfile(
    		@ModelAttribute("person") Person person, 
    		Model model, 
    		WebRequest request) {
    	String fwdPage = ApplicationUtility.forwardBasedOnSessionExisting(
    			isWebApplication, defaultPassword, signInErrMsg, userAccountService, 
    			model, USERPROFILE);
    	if(fwdPage != USERPROFILE){
    		return fwdPage;
    	}
    	
		final String username = System.getProperty("user.name");
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null) {
            return REDIRECT_SETUP;
        }

        if(person.getFirstName() == null){
            person = userAccount.getPerson();
            model.addAttribute("person", person);
            return USERPROFILE;
    	}
    	
        userAccount.setPerson(person);

        Path workspace = Paths.get(person.getWorkspaceDirectory());
        if (Files.exists(workspace)) {
            if (!Files.isDirectory(workspace)) {
                model.addAttribute("errorMsg", "Workspace provided is not a directory.");
                return USERPROFILE;
            }
        } else {
            model.addAttribute("errorMsg", "Workspace directory does not exist.");
            return USERPROFILE;
        }

        try {
            userAccount = userAccountService.saveUserAccount(userAccount);
        } catch (Exception exception) {
            model.addAttribute("errorMsg", setupErrMsg);
            return USERPROFILE;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(
        		userAccount.getUsername(), defaultPassword);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
        } catch (AuthenticationException exception) {
            model.addAttribute("errorMsg", signInErrMsg);
            return USERPROFILE;
        }

        ApplicationUtility.addAppUser2Model(isWebApplication, userAccount, model);
        request.setAttribute("successMsg", "Save your user profile successfully!", WebRequest.SCOPE_SESSION);
        
        return REDIRECT_USERPROFILE;
    }
    
}
