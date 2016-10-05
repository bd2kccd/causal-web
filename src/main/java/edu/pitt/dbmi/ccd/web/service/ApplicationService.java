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
package edu.pitt.dbmi.ccd.web.service;

import com.auth0.SessionUtils;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.LoginCredentials;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmResultService;
import edu.pitt.dbmi.ccd.web.service.algo.ResultComparisonService;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 4, 2016 12:20:21 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    public static final String INVALID_CREDENTIALS = "Invalid username and/or password.";
    public static final String UNACTIVATED_ACCOUNT = "Your account has not been activated.";
    public static final String LOGOUT_SUCCESS = "You Have Successfully Logged Out.";

    private final DataService dataService;
    private final AlgorithmResultService algorithmResultService;
    private final ResultComparisonService resultComparisonService;
    private final LoginService loginService;
    private final FileManagementService fileManagementService;
    private final AppUserService appUserService;
    private final UserAccountService userAccountService;

    @Autowired
    public ApplicationService(DataService dataService, AlgorithmResultService algorithmResultService, ResultComparisonService resultComparisonService, LoginService loginService, FileManagementService fileManagementService, AppUserService appUserService, UserAccountService userAccountService) {
        this.dataService = dataService;
        this.algorithmResultService = algorithmResultService;
        this.resultComparisonService = resultComparisonService;
        this.loginService = loginService;
        this.fileManagementService = fileManagementService;
        this.appUserService = appUserService;
        this.userAccountService = userAccountService;
    }

    public void retrieveFileCounts(AppUser appUser, Model model) {
        String username = appUser.getUsername();
        model.addAttribute("numOfDataset", dataService.countFiles(username));
        model.addAttribute("numOfAlgorithmResults", algorithmResultService.countFiles(username));
        model.addAttribute("numOfComparisonResults", resultComparisonService.countFiles(username));
    }

    public boolean logInUser(
            LoginCredentials loginCredentials,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request) {
        Subject currentUser = loginService.passwordLogin(loginCredentials);
        if (currentUser.isAuthenticated()) {
            String email = loginCredentials.getLoginUsername();
            UserAccount userAccount = userAccountService.findByEmail(email);
            if (userAccount.getActive()) {
                fileManagementService.createUserDirectories(userAccount);
                model.addAttribute("appUser", appUserService.createAppUser(userAccount, false));

                // update last login
                userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
                try {
                    userAccountService.save(userAccount);
                } catch (Exception exception) {
                    LOGGER.error("Unable to update last login.", exception);
                }

                return true;
            } else {
                currentUser.logout();
                redirectAttributes.addFlashAttribute("errorMsg", UNACTIVATED_ACCOUNT);
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", INVALID_CREDENTIALS);
            redirectAttributes.addFlashAttribute("loginCredentials", loginCredentials);
        }

        return false;
    }

    public void logOutUser(
            AppUser appUser,
            SessionStatus sessionStatus,
            RedirectAttributes redirectAttributes,
            HttpServletRequest req) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMsg", LOGOUT_SUCCESS);

            if (appUser.getFederatedUser()) {
                SessionUtils.setTokens(req, null);
                SessionUtils.setAuth0User(req, null);
            }
        }

        HttpSession httpSession = req.getSession();
        if (httpSession != null) {
            httpSession.invalidate();
        }
    }

}
