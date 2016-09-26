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
package edu.pitt.dbmi.ccd.web.service.account;

import edu.pitt.dbmi.ccd.db.domain.AccountRegistration;
import edu.pitt.dbmi.ccd.db.domain.UserRoleEnum;
import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserRole;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserRoleService;
import edu.pitt.dbmi.ccd.web.conf.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.domain.account.UserRegistration;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.EventLogService;
import edu.pitt.dbmi.ccd.web.service.mail.UserRegistrationMailService;
import edu.pitt.dbmi.ccd.web.util.UriTool;
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

    private static final String[] REGISTRATION_SUCCESS_NO_ACTIVATION = {"Registration Success!", "You may now sign in."};
    private static final String[] REGISTRATION_SUCCESS = {"Registration Success!", "You will receive a confirmation email soon."};
    private static final String[] REGISTRATION_FAILED = {"Registration Failed!", "Unable to register new user."};
    private static final String[] USERNAME_EXISTED = {"Registration Failed!", "Account already existed for that email."};

    private final CcdProperties ccdProperties;

    private final DefaultPasswordService passwordService;

    private final UserAccountService userAccountService;
    private final UserRoleService userRoleService;
    private final UserRegistrationMailService userRegistrationMailService;
    private final EventLogService eventLogService;

    @Autowired
    public UserRegistrationService(CcdProperties ccdProperties, DefaultPasswordService passwordService, UserAccountService userAccountService, UserRoleService userRoleService, UserRegistrationMailService userRegistrationMailService, EventLogService eventLogService) {
        this.ccdProperties = ccdProperties;
        this.passwordService = passwordService;
        this.userAccountService = userAccountService;
        this.userRoleService = userRoleService;
        this.userRegistrationMailService = userRegistrationMailService;
        this.eventLogService = eventLogService;
    }

    public void activateNewUser(String activationKey, HttpServletRequest request, RedirectAttributes redirectAttributes) throws ResourceNotFoundException {
        UserAccount userAccount = userAccountService.findByActivationKey(activationKey);
        if (userAccount == null || userAccount.isActivated()) {
            throw new ResourceNotFoundException();
        }
        userAccount.setActivated(true);
        userAccount.setActivationKey(null);

        try {
            userAccountService.save(userAccount);

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

    public void registerNewRegularUser(UserRegistration userRegistration, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (userAccountService.findByUsername(userRegistration.getUsername()) == null) {
            String username = userRegistration.getUsername();
            String password = passwordService.encryptPassword(userRegistration.getPassword());
            boolean activated = !ccdProperties.isRequireActivation();
            String email = userRegistration.getUsername();
            String firstName = userRegistration.getFirstName();
            String lastName = userRegistration.getLastName();
            String workspace = ccdProperties.getWorkspaceDir();

            AccountRegistration registration = new AccountRegistration();
            registration.setActivated(activated);
            registration.setEmail(email);
            registration.setFirstName(firstName);
            registration.setLastName(lastName);
            registration.setPassword(password);
            registration.setUsername(username);
            registration.setWorkspace(workspace);

            UserRole userRole = userRoleService.findByEnum(UserRoleEnum.USER);

            UserAccount userAccount = null;
            try {
                userAccount = userAccountService.createNewAccount(registration, userRole);
            } catch (Exception exception) {
                LOGGER.error("Failed to register new user.", exception);
            }

            if (userAccount == null) {
                redirectAttributes.addFlashAttribute("errorMsg", REGISTRATION_FAILED);
            } else {
                eventLogService.userRegistration(userAccount);
                if (userAccount.isActivated()) {
                    redirectAttributes.addFlashAttribute("successMsg", REGISTRATION_SUCCESS_NO_ACTIVATION);
                } else {
                    // send e-mail notification to user
                    String activationLink = createActivationLink(userAccount, request);
                    try {
                        sendOutActivationLink(userAccount, activationLink);
                    } catch (Exception exception) {
                        LOGGER.error("Failed to send new-user-registration notifications.", exception);
                    }
                    redirectAttributes.addFlashAttribute("successMsg", REGISTRATION_SUCCESS);
                }
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

    protected UserAccount createUserAccount(UserRegistration userRegistration, HttpServletRequest request) {
        String username = userRegistration.getUsername();
        String password = userRegistration.getPassword();

        boolean activated = !ccdProperties.isRequireActivation();
        String activationKey = activated ? null : UUID.randomUUID().toString();
        String account = UUID.randomUUID().toString();
        String encodePassword = passwordService.encryptPassword(password);

        Person person = createPerson(userRegistration, account);

        UserAccount userAccount = new UserAccount();
        userAccount.setAccount(account);
        userAccount.setActivationKey(activationKey);
        userAccount.setActivated(activated);
        userAccount.setPassword(encodePassword);
        userAccount.setPerson(person);
        userAccount.setRegistrationDate(new Date(System.currentTimeMillis()));
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
