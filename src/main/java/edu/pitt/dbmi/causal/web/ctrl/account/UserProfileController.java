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

import edu.pitt.dbmi.causal.web.ctrl.ViewPath;
import edu.pitt.dbmi.causal.web.model.AppUser;
import edu.pitt.dbmi.causal.web.model.account.ChangePasswordForm;
import edu.pitt.dbmi.causal.web.model.account.UserInfoForm;
import edu.pitt.dbmi.causal.web.service.account.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jun 30, 2016 3:48:46 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/account/profile")
public class UserProfileController {

    private static final String PASSWORD_MATCH_FAILED = "Password Update Failed!";
    private static final String[] USER_PROFILE_UPDATE_SUCCESS = {"User information has been updated successfully."};
    private static final String[] USER_PROFILE_UPDATE_FAILED = {"User Profile Update Failed!", "Unable to update user information."};
    private static final String[] PASSWORD_UPDATE_SUCCESS = {"Password has been changed successfully."};
    private static final String[] PASSWORD_UPDATE_FAILED = {"Password Update Failed!", "Unable to change password."};

    private final UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("password/change")
    public String processPasswordChange(
            @ModelAttribute("changePasswordForm") final ChangePasswordForm changePasswordForm,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        if (userProfileService.passwordMatch(changePasswordForm, appUser)) {
            if (userProfileService.updateUserPassword(changePasswordForm, appUser)) {
                redirAttrs.addFlashAttribute("successMsg", PASSWORD_UPDATE_SUCCESS);
            } else {
                redirAttrs.addFlashAttribute("errorMsg", PASSWORD_UPDATE_FAILED);
            }
        } else {
            redirAttrs.addFlashAttribute("errorMsg", PASSWORD_MATCH_FAILED);
            redirAttrs.addFlashAttribute("errInvalidPwd", true);
        }

        return ViewPath.REDIRECT_USER_PROFILE;
    }

    @PostMapping("info/change")
    public String processUserInfoChange(
            @ModelAttribute("userInfoForm") final UserInfoForm userInfoForm,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        AppUser updatedAppUser = userProfileService.updateUserInformation(userInfoForm, appUser);
        if (updatedAppUser == null) {
            redirAttrs.addFlashAttribute("errorMsg", USER_PROFILE_UPDATE_FAILED);
        } else {
            model.addAttribute("appUser", appUser);
            redirAttrs.addFlashAttribute("successMsg", USER_PROFILE_UPDATE_SUCCESS);
        }

        return ViewPath.REDIRECT_USER_PROFILE;
    }

    @GetMapping
    public String showUserProfilePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        if (!model.containsAttribute("userInfoForm")) {
            model.addAttribute("userInfoForm", userProfileService.createUserInfoForm(appUser));
        }
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }

        return ViewPath.USER_PROFILE_VIEW;
    }

}
