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
package edu.pitt.dbmi.ccd.web.ctrl.user;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.user.PasswordChange;
import edu.pitt.dbmi.ccd.web.domain.user.UserInfo;
import edu.pitt.dbmi.ccd.web.service.user.UserProfileService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
@RequestMapping(value = "secured/user/profile")
public class UserProfileController implements ViewPath {

    private final UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showUserProfilePage(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        if (!model.containsAttribute("userInfo")) {
            model.addAttribute("userInfo", userProfileService.createUserInfo(appUser));
        }
        if (!model.containsAttribute("passwordChange")) {
            model.addAttribute("passwordChange", new PasswordChange());
        }

        return USER_PROFILE_VIEW;
    }

    @RequestMapping(value = "info", method = RequestMethod.POST)
    public String updateUserInfo(
            @ModelAttribute("userInfo") final UserInfo userInfo,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model,
            final RedirectAttributes redirectAttributes) {
        userProfileService.updateUserProfile(userInfo, appUser, model, redirectAttributes);

        return REDIRECT_USER_PROFILE;
    }

    @RequestMapping(value = "pwd", method = RequestMethod.POST)
    public String updateUserPassword(
            @Valid @ModelAttribute("passwordChange") final PasswordChange passwordChange,
            final BindingResult bindingResult,
            @ModelAttribute("appUser") final AppUser appUser,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordChange", bindingResult);
            redirectAttributes.addFlashAttribute("passwordChange", passwordChange);
            redirectAttributes.addFlashAttribute("pwdChangeErr", true);
        } else {
            userProfileService.updateUserPassword(passwordChange, appUser, redirectAttributes);
        }

        return REDIRECT_USER_PROFILE;
    }

}
