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

package edu.pitt.dbmi.ccd.web.util;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.ui.Model;

import edu.pitt.dbmi.ccd.db.entity.Person;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.web.ctrl.ViewController;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.service.UserAccountService;

/**
 *
 * May 29, 2015 12:32:20 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 * 
 */
public class ApplicationUtility implements ViewController {
    
    public static void addAppUser2Model(
    		boolean isWebApplication,
    		UserAccount userAccount, 
    		Model model){
        final String username = userAccount.getUsername();
        final Person person = userAccount.getPerson();
    	
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setWebUser(isWebApplication);
        appUser.setCreatedDate(FileUtility.formatDate(userAccount.getCreatedDate()));
        appUser.setLastLoginDate(FileUtility.formatDate(userAccount.getLastLoginDate()));
        appUser.setPerson(person);
        model.addAttribute("appUser", appUser);
    }

    public static String forwardBasedOnSessionExisting(
    		boolean isWebApplication, 
    		String defaultPassword,
    		String signInErrMsg,
    		UserAccountService userAccountService, 
    		Model model){
	   	 if (!SecurityUtils.getSubject().isAuthenticated()) {
			 if(isWebApplication){
	             return LOGIN;
			 } else {
	             String username = System.getProperty("user.name");
	             UserAccount userAccount = userAccountService.findByUsername(username);
	             if (userAccount == null) {
	                 return REDIRECT_SETUP;
	             }
	
	             UsernamePasswordToken token = new UsernamePasswordToken(
	            		 userAccount.getUsername(), defaultPassword);
	             token.setRememberMe(true);
	             Subject currentUser = SecurityUtils.getSubject();
	             try {
	                 currentUser.login(token);
	             } catch (AuthenticationException exception) {
	                 model.addAttribute("errorMsg", signInErrMsg);
	                 return REDIRECT_SETUP;
	             }
	
	             userAccount.setLastLoginDate(new Date(System.currentTimeMillis()));
	             userAccount = userAccountService.saveUserAccount(userAccount);
	             
	             addAppUser2Model(isWebApplication, userAccount, model);
			 }
			 
		 }
	   	 return null;
    }

}
