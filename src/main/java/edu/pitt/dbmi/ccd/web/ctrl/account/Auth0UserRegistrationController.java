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
package edu.pitt.dbmi.ccd.web.ctrl.account;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import static edu.pitt.dbmi.ccd.web.ctrl.ViewPath.REDIRECT_HOME;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.Auth0LoginService;
import edu.pitt.dbmi.ccd.web.service.account.Auth0UserRegistrationService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 29, 2016 3:01:22 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("auth0")
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "auth0/user/registration")
public class Auth0UserRegistrationController implements ViewPath {

    private final Auth0UserRegistrationService auth0UserRegistrationService;
    private final Auth0LoginService auth0LoginService;

    @Autowired
    public Auth0UserRegistrationController(Auth0UserRegistrationService auth0UserRegistrationService, Auth0LoginService auth0LoginService) {
        this.auth0UserRegistrationService = auth0UserRegistrationService;
        this.auth0LoginService = auth0LoginService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerNewUser(
            @RequestParam("agree") boolean agree,
            @ModelAttribute("appUser") final AppUser appUser,
            final SessionStatus sessionStatus,
            final HttpServletRequest req,
            final HttpServletResponse res,
            final RedirectAttributes redirectAttributes,
            final Model model) {
        if (agree) {
            UserAccount userAccount = auth0UserRegistrationService.registerNewUser(appUser, redirectAttributes, req);
            auth0LoginService.logInUser(userAccount, null, req, res, model);

            return REDIRECT_HOME;
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "You must accept the terms.");
            sessionStatus.setComplete();

            return REDIRECT_LOGIN;
        }
    }

}
