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
package edu.pitt.dbmi.ccd.web.prop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * May 26, 2016 1:27:41 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PropertySource("classpath:ccd.properties")
public class CcdEmailProperties {

    @Value("${ccd.email.admin.sendto}")
    private String[] adminSendTo;

    @Value("${ccd.email.acct.reg.user.confirm.subject:New Registration Confirmation}")
    private String acctRegUserConfirmSubject;

    @Value("${ccd.email.acct.reg.admin.activ.subject:New User Registration}")
    private String acctRegAdminActivSubject;

    @Value("${ccd.email.acct.reg.user.activ.subject:New User Registration and Activation}")
    private String acctRegUserActivSubject;

    @Value("${ccd.email.acct.reg.user.activ.sucess.subject:Account Activated}")
    private String acctRegUserActivSuccessSubject;

    @Value("${ccd.email.acct.pwd.reset.subject:Password Reset}")
    private String acctPwdResetSubject;

    public CcdEmailProperties() {
    }

    public String[] getAdminSendTo() {
        return adminSendTo;
    }

    public void setAdminSendTo(String[] adminSendTo) {
        this.adminSendTo = adminSendTo;
    }

    public String getAcctRegUserConfirmSubject() {
        return acctRegUserConfirmSubject;
    }

    public void setAcctRegUserConfirmSubject(String acctRegUserConfirmSubject) {
        this.acctRegUserConfirmSubject = acctRegUserConfirmSubject;
    }

    public String getAcctRegAdminActivSubject() {
        return acctRegAdminActivSubject;
    }

    public void setAcctRegAdminActivSubject(String acctRegAdminActivSubject) {
        this.acctRegAdminActivSubject = acctRegAdminActivSubject;
    }

    public String getAcctRegUserActivSubject() {
        return acctRegUserActivSubject;
    }

    public void setAcctRegUserActivSubject(String acctRegUserActivSubject) {
        this.acctRegUserActivSubject = acctRegUserActivSubject;
    }

    public String getAcctRegUserActivSuccessSubject() {
        return acctRegUserActivSuccessSubject;
    }

    public void setAcctRegUserActivSuccessSubject(String acctRegUserActivSuccessSubject) {
        this.acctRegUserActivSuccessSubject = acctRegUserActivSuccessSubject;
    }

    public String getAcctPwdResetSubject() {
        return acctPwdResetSubject;
    }

    public void setAcctPwdResetSubject(String acctPwdResetSubject) {
        this.acctPwdResetSubject = acctPwdResetSubject;
    }

}
