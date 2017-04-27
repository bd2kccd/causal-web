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
package edu.pitt.dbmi.ccd.web.ctrl.account;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.account.PasswordChangeForm;
import edu.pitt.dbmi.ccd.web.domain.account.UserInfoForm;
import edu.pitt.dbmi.ccd.web.service.AppUserService;
import edu.pitt.dbmi.ccd.web.service.account.UserProfileService;
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
 * Jun 30, 2016 3:48:46 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "secured/user/account/profile")
public class UserProfileController implements ViewPath {

    private static final String[] USER_PROFILE_UPDATE_FAILED = {"User Profile Update Failed!", "Unable to update user information."};
    private static final String[] PASSWORD_UPDATE_FAILED = {"Password Update Failed!", "Unable to change password."};
    private static final String[] PASSWORD_MATCH_FAILED = {"Password Update Failed!"};

    private final UserProfileService userProfileService;
    private final AppUserService appUserService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService, AppUserService appUserService) {
        this.userProfileService = userProfileService;
        this.appUserService = appUserService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
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
        if (userAccount == null) {
            redirAttrs.addFlashAttribute("errorMsgUserInfo", USER_PROFILE_UPDATE_FAILED);
        } else {
            if (userProfileService.updateUserInfo(userInfoForm, userAccount)) {
                redirAttrs.addFlashAttribute("appUser", appUserService.update(userInfoForm, appUser));
            } else {
                redirAttrs.addFlashAttribute("errorMsgUserInfo", USER_PROFILE_UPDATE_FAILED);
            }
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
