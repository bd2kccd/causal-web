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

import edu.pitt.dbmi.ccd.db.domain.EventTypeName;
import edu.pitt.dbmi.ccd.db.entity.EventType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.EventTypeService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.UserEventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Aug 10, 2016 3:53:37 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class EventLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogService.class);

    private final EventTypeService eventTypeService;
    private final UserEventLogService userEventLogService;
    private final UserAccountService userAccountService;

    @Autowired
    public EventLogService(EventTypeService eventTypeService, UserEventLogService userEventLogService, UserAccountService userAccountService) {
        this.eventTypeService = eventTypeService;
        this.userEventLogService = userEventLogService;
        this.userAccountService = userAccountService;
    }

    public void logUserProfileChange(UserAccount userAccount, Long location) {
        logUserEvent(EventTypeName.USER_PROFILE_CHANGE, userAccount, location);
    }

    public void logUserPasswordChange(UserAccount userAccount, Long location) {
        logUserEvent(EventTypeName.USER_PASSWORD_CHANGE, userAccount, location);
    }

    public void logUserSignIn(UserAccount userAccount, Long location) {
        logUserEvent(EventTypeName.USER_LOGIN, userAccount, location);
    }

    public void logUserSignInFailed(String username, Long location) {
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount != null) {
            logUserEvent(EventTypeName.USER_LOGIN_FAILED, userAccount, location);
        }
    }

    public void logUserSignOut(UserAccount userAccount, Long location) {
        logUserEvent(EventTypeName.USER_LOGOUT, userAccount, location);
    }

    public void logUserRegistration(UserAccount userAccount) {
        logUserEvent(EventTypeName.USER_REGISTRATION, userAccount, userAccount.getRegistrationLocation());
    }

    public void logUserEvent(EventTypeName eventTypeName, UserAccount userAccount, Long eventLocation) {
        if (eventTypeName == null || userAccount == null) {
            return;
        }

        try {
            EventType eventType = eventTypeService.findByEventTypeName(eventTypeName);
            userEventLogService.logUserEvent(eventType, userAccount, eventLocation);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
    }

}
