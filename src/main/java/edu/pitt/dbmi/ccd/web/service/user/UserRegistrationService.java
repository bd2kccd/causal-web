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
package edu.pitt.dbmi.ccd.web.service.user;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.entity.UserLoginAttempt;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserRoleService;
import edu.pitt.dbmi.ccd.web.domain.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.mail.UserRegistrationMailService;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Apr 24, 2016 11:31:50 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class UserRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationService.class);

    private static final String[] REGISTRATION_SUCCESS = {"Registration Success!", "You will receive a confirmation email soon."};
    private static final String[] REGISTRATION_FAILED = {"Registration Failed!", "Unable to register new user."};
    private static final String[] USERNAME_EXISTED = {"Registration Failed!", "Account already existed for that email."};

    private final CcdProperties ccdProperties;

    private final DefaultPasswordService passwordService;

    private final UserAccountService userAccountService;

    private final UserRoleService userRoleService;

    private final UserRegistrationMailService userRegistrationMailService;

    @Autowired
    public UserRegistrationService(CcdProperties ccdProperties, DefaultPasswordService passwordService, UserAccountService userAccountService, UserRoleService userRoleService, UserRegistrationMailService userRegistrationMailService) {
        this.ccdProperties = ccdProperties;
        this.passwordService = passwordService;
        this.userAccountService = userAccountService;
        this.userRoleService = userRoleService;
        this.userRegistrationMailService = userRegistrationMailService;
    }

    public void activateNewUser(String activationKey, HttpServletRequest request, RedirectAttributes redirectAttributes) throws ResourceNotFoundException {
        UserAccount userAccount = userAccountService.findByActivationKey(activationKey);
        if (userAccount == null || userAccount.isActive()) {
            throw new ResourceNotFoundException();
        }
        userAccount.setActive(true);
        userAccount.setActivationKey(null);

        try {
            userAccountService.saveUserAccount(userAccount);

            // send e-mail notification to user
            String url = UriTool.buildURI(request, ccdProperties).build().toString();
            userRegistrationMailService.sendUserActivationSuccess(userAccount, url);

            redirectAttributes.addFlashAttribute("header", "User Activation Success!");
            redirectAttributes.addFlashAttribute("successMsg", String.format("You have successfully activated user '%s'.", userAccount.getUsername()));
        } catch (Exception exception) {
            LOGGER.error(String.format("Unable to activate user account '%s'.", userAccount.getUsername()), exception);

            redirectAttributes.addFlashAttribute("header", "User Activation Failed!");
            redirectAttributes.addFlashAttribute("errorMsg", String.format("Unable to activate user '%s'.", userAccount.getUsername()));
        }
    }

    public void registerNewUser(UserRegistration userRegistration, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (userAccountService.findByUsername(userRegistration.getUsername()) == null) {
            String userIPAddress = request.getRemoteAddr();

            // create a user account in the database
            UserAccount userAccount = createUserAccount(userRegistration, userIPAddress);
            try {
                userAccount = userAccountService.saveUserAccount(userAccount);

                String activationLink = createActivationLink(userAccount, request);
                sendOutActivationLink(userAccount, activationLink);

                redirectAttributes.addFlashAttribute("successMsg", REGISTRATION_SUCCESS);
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
                redirectAttributes.addFlashAttribute("errorMsg", REGISTRATION_FAILED);
            }
        } else {
            redirectAttributes.addFlashAttribute("userRegistration", userRegistration);
            redirectAttributes.addFlashAttribute("errorMsg", USERNAME_EXISTED);
        }
    }

    protected String createActivationLink(UserAccount userAccount, HttpServletRequest request) {
        String activationKey = userAccount.getActivationKey();
        String activationLink = UriTool.buildURI(request, ccdProperties)
                .pathSegment("user", "account", "registration", "activate")
                .queryParam("activation", Base64.getUrlEncoder().encodeToString(activationKey.getBytes()))
                .build().toString();

        return activationLink;
    }

    protected void sendOutActivationLink(UserAccount userAccount, String activationLink) {
        if (ccdProperties.isAccountSelfActivation()) {
            userRegistrationMailService.sendUserSelfActivation(userAccount, activationLink);
        } else {
            userRegistrationMailService.sendUserNewAccountConfirmation(userAccount);
            userRegistrationMailService.sendAdminUserActivation(userAccount, activationLink);
        }
    }

    protected UserAccount createUserAccount(UserRegistration userRegistration, String userIPAddress) {
        String username = userRegistration.getUsername();
        String password = userRegistration.getPassword();
        String account = UUID.randomUUID().toString();
        String activationKey = UUID.randomUUID().toString();
        Person person = createPerson(userRegistration, account);

        Long registrationLocation;
        try {
            registrationLocation = UriTool.InetNTOA(userIPAddress);
        } catch (UnknownHostException exception) {
            LOGGER.error(exception.getLocalizedMessage());
            registrationLocation = null;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setAccount(account);
        userAccount.setActivationKey(activationKey);
        userAccount.setActive(false);
        userAccount.setDisabled(false);
        userAccount.setPassword(passwordService.encryptPassword(password));
        userAccount.setPerson(person);
        userAccount.setRegistrationDate(new Date(System.currentTimeMillis()));
        userAccount.setRegistrationLocation(registrationLocation);
        userAccount.setUserLogin(new UserLogin());
        userAccount.setUserLoginAttempt(new UserLoginAttempt());
        userAccount.setUserRole(userRoleService.findByName("user"));
        userAccount.setUsername(username);

        return userAccount;
    }

    protected Person createPerson(UserRegistration userRegistration, String account) {
        String firstName = userRegistration.getFirstName();
        String lastName = userRegistration.getLastName();
        String email = userRegistration.getUsername();
        Path workspace = Paths.get(ccdProperties.getWorkspaceDir(), account.replace("-", "_"));

        Person person = new Person();
        person.setFirstName((firstName == null) ? "" : firstName);
        person.setLastName((lastName == null) ? "" : lastName);
        person.setEmail(email);
        person.setWorkspace(workspace.toAbsolutePath().toString());

        return person;
    }

}
