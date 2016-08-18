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
 * Feb 13, 2016 2:44:02 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Component
@PropertySource("classpath:ccd.properties")
public class CcdProperties {

    @Value("${ccd.server.hostname:}")
    private String serverHostName;

    @Value("${ccd.server.port:}")
    private String serverPort;

    @Value("${ccd.dir.workspace:}")
    private String workspaceDir;

    @Value("${ccd.folder.data}")
    private String dataFolder;

    @Value("${ccd.folder.lib}")
    private String libFolder;

    @Value("${ccd.folder.tmp}")
    private String tmpFolder;

    @Value("${ccd.folder.result}")
    private String resultFolder;

    @Value("${ccd.app.title}")
    private String title;

    @Value("${ccd.app.copyright}")
    private String copyright;

    @Value("${ccd.app.agreement}")
    private String agreement;

    @Value("${ccd.acct.reg.activ.required:false}")
    private boolean requireActivation;

    @Value("${ccd.acct.reg.activ.self:false}")
    private boolean accountSelfActivation;

    public CcdProperties() {
    }

    public String getServerHostName() {
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public String getLibFolder() {
        return libFolder;
    }

    public void setLibFolder(String libFolder) {
        this.libFolder = libFolder;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(String tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public String getResultFolder() {
        return resultFolder;
    }

    public void setResultFolder(String resultFolder) {
        this.resultFolder = resultFolder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public boolean isRequireActivation() {
        return requireActivation;
    }

    public void setRequireActivation(boolean requireActivation) {
        this.requireActivation = requireActivation;
    }

    public boolean isAccountSelfActivation() {
        return accountSelfActivation;
    }

    public void setAccountSelfActivation(boolean accountSelfActivation) {
        this.accountSelfActivation = accountSelfActivation;
    }

}
