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
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.SessionAttributes;

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

    private final UserAccountService userAccountService;
    private final AppUserService appUserService;

    @Autowired
    public Auth0LoginController(UserAccountService userAccountService, AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.appUserService = appUserService;
    }

}
