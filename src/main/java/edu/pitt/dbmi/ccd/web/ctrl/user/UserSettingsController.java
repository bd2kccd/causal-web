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

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.user.UserLogin;
import edu.pitt.dbmi.ccd.web.service.user.settings.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Oct 7, 2015 3:59:56 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "user/settings")
public class UserSettingsController implements ViewPath {

    private final UserSettingsService userSettingsService;

    private final UserAccountService userAccountService;

    @Autowired(required = true)
    public UserSettingsController(UserSettingsService userSettingsService, UserAccountService userAccountService) {
        this.userSettingsService = userSettingsService;
        this.userAccountService = userAccountService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showUserSettings(
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        String accountId = userAccount.getAccountId();
        if (accountId == null) {
            model.addAttribute("userLogin", new UserLogin());
        } else {
            model.addAttribute("accountId", accountId);
        }

        return USER_SETTINGS_VIEW;
    }

    @RequestMapping(value = "account/remove", method = RequestMethod.GET)
    public String removeWebAccount(@ModelAttribute("appUser") final AppUser appUser) {
        userSettingsService.deleteWebAcount(appUser.getUsername());

        return REDIRECT_USER_SETTINGS;
    }

    @RequestMapping(value = "account", method = RequestMethod.POST)
    public String addWebAccount(
            @ModelAttribute("userLogin") final UserLogin userLogin,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes) {
        if (!userSettingsService.addWebAccount(userLogin.getUsr(), userLogin.getPwd(), appUser.getUsername())) {
            redirectAttributes.addFlashAttribute("userLoginErr", "Unable to add web account.");
        }

        return REDIRECT_USER_SETTINGS;
    }

}
