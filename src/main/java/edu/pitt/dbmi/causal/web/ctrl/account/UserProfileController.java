/*
 * Copyright (C) 2017 University of Pittsburgh.
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
import edu.pitt.dbmi.causal.web.model.account.PasswordChangeForm;
import edu.pitt.dbmi.causal.web.model.account.UserInfoForm;
import edu.pitt.dbmi.causal.web.service.AppUserService;
import edu.pitt.dbmi.causal.web.service.account.UserProfileService;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class UserProfileController implements ViewPath {

    private static final String PASSWORD_MATCH_FAILED = "Password Update Failed!";
    private static final String[] USER_PROFILE_UPDATE_FAILED = {"User Profile Update Failed!", "Unable to update user information."};
    private static final String[] PASSWORD_UPDATE_FAILED = {"Password Update Failed!", "Unable to change password."};

    private final UserAccountService userAccountService;
    private final UserProfileService userProfileService;
    private final AppUserService appUserService;

    @Autowired
    public UserProfileController(UserAccountService userAccountService, UserProfileService userProfileService, AppUserService appUserService) {
        this.userAccountService = userAccountService;
        this.userProfileService = userProfileService;
        this.appUserService = appUserService;
    }

    @RequestMapping(value = "password/change", method = RequestMethod.POST)
    public String processPasswordChange(
            @ModelAttribute("passwordChangeForm") final PasswordChangeForm passwordChangeForm,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userAccount == null) {
            redirAttrs.addFlashAttribute("errorMsgPassword", PASSWORD_UPDATE_FAILED);
        } else {
            if (userProfileService.passwordMatch(passwordChangeForm, userAccount)) {
                if (!userProfileService.updateUserAccountPassword(passwordChangeForm, userAccount)) {
                    redirAttrs.addFlashAttribute("errorMsgPassword", PASSWORD_UPDATE_FAILED);
                }
            } else {
                redirAttrs.addFlashAttribute("errorMsgPassword", PASSWORD_MATCH_FAILED);
                redirAttrs.addFlashAttribute("errInvalidPwd", true);
            }
        }

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(value = "info/change", method = RequestMethod.POST)
    public String processUserInfoChange(
            @ModelAttribute("userInfoForm") final UserInfoForm userInfoForm,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirAttrs) {
        UserAccount userAccount = appUserService.retrieveUserAccount(appUser);
        if (userProfileService.updateUserInfo(userInfoForm, userAccount)) {
            userAccount = userAccountService.getRepository().findOne(userAccount.getId());
            model.addAttribute("appUser", appUserService.create(userAccount, appUser.isFederatedUser()));
        } else {
            redirAttrs.addFlashAttribute("errorMsgUserInfo", USER_PROFILE_UPDATE_FAILED);
        }

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showUserProfilePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        if (!model.containsAttribute("userInfoForm")) {
            model.addAttribute("userInfoForm", userProfileService.populateUserInfoForm(appUser));
        }
        if (!model.containsAttribute("passwordChangeForm")) {
            model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        }

        return USER_PROFILE_VIEW;
    }

}
