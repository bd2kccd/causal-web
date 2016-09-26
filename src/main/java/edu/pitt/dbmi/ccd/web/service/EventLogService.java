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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.db.domain.EventTypeEnum;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.UserEventLog;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserEventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 9, 2016 2:15:36 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class EventLogService {

    private final UserEventLogService userEventLogService;
    private final UserAccountService userAccountService;

    @Autowired
    public EventLogService(UserEventLogService userEventLogService, UserAccountService userAccountService) {
        this.userEventLogService = userEventLogService;
        this.userAccountService = userAccountService;
    }

    public UserEventLog userProfileUpdate(UserAccount userAccount) {
        return userEventLogService.logUserEvent(EventTypeEnum.USER_PROFILE_UPDATE, userAccount);
    }

    public UserEventLog userRegistration(UserAccount userAccount) {
        return userEventLogService.logUserEvent(EventTypeEnum.USER_REGISTRATION, userAccount);
    }

    public UserEventLog userLogIn(UserAccount userAccount) {
        return userEventLogService.logUserEvent(EventTypeEnum.USER_LOGIN, userAccount);
    }

    public UserEventLog userLogOut(UserAccount userAccount) {
        return userEventLogService.logUserEvent(EventTypeEnum.USER_LOGOUT, userAccount);
    }

    public void userLogInFailed(String username) {
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount != null) {
            userEventLogService.logUserEvent(EventTypeEnum.USER_LOGIN_FAILED, userAccount);
        }
    }

}
