/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.ctrl.account;

import edu.pitt.dbmi.causal.web.ctrl.SitePaths;
import edu.pitt.dbmi.causal.web.ctrl.SiteViews;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.PasswordUpdateForm;
import edu.pitt.dbmi.causal.web.model.account.UserProfileUpdateForm;
import edu.pitt.dbmi.causal.web.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jul 6, 2018 2:18:42 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/account")
public class AccountController {

    private static final String[] USER_PROFILE_UPDATE_SUCCESS = {"User information has been updated successfully."};
    private static final String[] USER_PROFILE_UPDATE_FAILED = {"User Profile Update Failed!", "Unable to update user information."};
    private static final String PASSWORD_MATCH_FAILED = "Password Update Failed!";
    private static final String[] PASSWORD_UPDATE_SUCCESS = {"Password has been changed successfully."};
    private static final String[] PASSWORD_UPDATE_FAILED = {"Password Update Failed!", "Unable to change password."};

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping("password")
    public String updateUserProfile(
            @ModelAttribute("passwordUpdateForm") final PasswordUpdateForm passwordUpdateForm,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (accountService.passwordMatch(passwordUpdateForm, appUser)) {
            if (accountService.updateUserPassword(passwordUpdateForm, appUser)) {
                redirAttrs.addFlashAttribute("successMsg", PASSWORD_UPDATE_SUCCESS);
            } else {
                redirAttrs.addFlashAttribute("errorMsg", PASSWORD_UPDATE_FAILED);
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", PASSWORD_MATCH_FAILED);
            redirAttrs.addFlashAttribute("errInvalidPwd", true);
        }

        return SitePaths.REDIRECT_USER_ACCOUNT;
    }

    @PostMapping("profile")
    public String updateUserProfile(
            @ModelAttribute("userProfileUpdateForm") final UserProfileUpdateForm userProfileUpdateForm,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        AppUser updatedAppUser = accountService.updateUserProfile(userProfileUpdateForm, appUser);
        if (updatedAppUser == null) {
            redirAttrs.addFlashAttribute("errorMsg", USER_PROFILE_UPDATE_FAILED);
        } else {
            model.addAttribute("appUser", appUser);
            redirAttrs.addFlashAttribute("successMsg", USER_PROFILE_UPDATE_SUCCESS);
        }

        return SitePaths.REDIRECT_USER_ACCOUNT;
    }

    @GetMapping
    public String showUserProfilePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        if (!model.containsAttribute("userProfileUpdateForm")) {
            model.addAttribute("userProfileUpdateForm", accountService.createUserProfileUpdateForm(appUser));
        }
        if (!model.containsAttribute("passwordUpdateForm")) {
            model.addAttribute("passwordUpdateForm", new PasswordUpdateForm());
        }

        return SiteViews.USER_ACCOUNT;
    }

}
