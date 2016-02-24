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
package edu.pitt.dbmi.ccd.web.ctrl;

import com.auth0.NonceGenerator;
import com.auth0.NonceStorage;
import com.auth0.RequestNonceStorage;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserLogin;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.LOGIN;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_HOME;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_LOGIN;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.util.UrlUtility;
import java.net.UnknownHostException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 * Feb 18, 2016 2:00:19 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@Profile("auth0")
@SessionAttributes("appUser")
public class Auth0LoginController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0LoginController.class);

    private final NonceGenerator nonceGenerator = new NonceGenerator();

    public static final String LOGIN_VIEW = "auth0Login";

    private final CcdProperties ccdProperties;

    private final UserAccountService userAccountService;

    private final AppUserService appUserService;

    @Autowired
    public Auth0LoginController(CcdProperties ccdProperties, UserAccountService userAccountService, AppUserService appUserService) {
        this.ccdProperties = ccdProperties;
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.GET)
    public String showLoginPage(
            final HttpServletRequest request,
            final SessionStatus sessionStatus,
            final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        if (sessionStatus.isComplete()) {
            currentUser.logout();
        } else if (currentUser.isAuthenticated()) {
            return REDIRECT_HOME;
        } else {
            sessionStatus.setComplete();
        }

        String nonce = nonceGenerator.generateNonce();
        NonceStorage nonceStorage = new RequestNonceStorage(request);
        nonceStorage.setState(nonce);

        model.addAttribute("callbackUrl", ccdProperties.getServerURL() + "/callback");
        model.addAttribute("state", nonce);
        model.addAttribute("userRegistration", new UserRegistration());

        return LOGIN_VIEW;
    }

    @RequestMapping(value = LOGIN, method = RequestMethod.POST)
    public String processLogin(
            final HttpServletRequest request,
            final UsernamePasswordToken credentials,
            final RedirectAttributes redirectAttributes,
            final Model model) {
        Subject currentUser = SecurityUtils.getSubject();
        String username = credentials.getUsername();
        try {
            currentUser.login(credentials);
        } catch (AuthenticationException exception) {
            LOGGER.warn(String.format("Failed login attempt from user %s.", username));
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid username and/or password.");
            return REDIRECT_LOGIN;
        }

        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount.isActive()) {
            UserLogin userLogin = userAccount.getUserLogin();
            userLogin.setLastLoginDate(userLogin.getLoginDate());
            userLogin.setLastLoginLocation(userLogin.getLoginLocation());
            userLogin.setLoginDate(new Date(System.currentTimeMillis()));
            try {
                userLogin.setLoginLocation(UrlUtility.InetNTOA(request.getRemoteAddr()));
            } catch (UnknownHostException exception) {
                LOGGER.info(exception.getLocalizedMessage());
            }
            userAccountService.saveUserAccount(userAccount);

            model.addAttribute("appUser", appUserService.createAppUser(userAccount));
            return REDIRECT_HOME;
        } else {
            currentUser.logout();
            redirectAttributes.addFlashAttribute("errorMsg", "Your account has not been activated.");
            return REDIRECT_LOGIN;
        }
    }

    @RequestMapping(value = LOGOUT, method = RequestMethod.GET)
    public RedirectView logOut(
            @Value("${auth0.domain}") final String auth0Domain,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final SessionStatus sessionStatus,
            final RedirectAttributes redirectAttributes) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMsg", "You have successfully logged out.");
        }

        if (appUser.getLocalAccount()) {
            return new RedirectView(LOGIN, true);
        } else {
            String ccdLogOut = ccdProperties.getServerURL() + "/" + LOGIN;
            String redirect = String.format("https://%s/v2/logout?returnTo=%s", auth0Domain, ccdLogOut);
            return new RedirectView(redirect, false);
        }
    }

}
