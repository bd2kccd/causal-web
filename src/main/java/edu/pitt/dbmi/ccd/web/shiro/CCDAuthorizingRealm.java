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
package edu.pitt.dbmi.ccd.web.shiro;

import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * May 14, 2015 1:57:20 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
public class CCDAuthorizingRealm extends AuthorizingRealm {

    @Autowired(required = true)
    private UserAccountService userAccountService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        final String username = (String) principalCollection.getPrimaryPrincipal();
        final UserAccount userAccount = userAccountService.findByEmail(username);
        if (userAccount == null) {
            throw new UnknownAccountException("Account does not exist");
        }

        Set<String> roles = new LinkedHashSet<>();
        roles.add("admin");
        Set<String> permissions = new LinkedHashSet<>();
        permissions.add("*");

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo(roles);
        authorizationInfo.setStringPermissions(permissions);

        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        final UsernamePasswordToken credentials = (UsernamePasswordToken) token;
        final String username = credentials.getUsername();
        if (username == null) {
            throw new UnknownAccountException("Username not provided");
        }
        final UserAccount userAccount = userAccountService.findByEmail(username);
        if (userAccount == null) {
            throw new UnknownAccountException("Account does not exist");
        }

        return new SimpleAuthenticationInfo(username, userAccount.getPassword().toCharArray(), getName());
    }

}
