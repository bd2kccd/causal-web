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

    @Value("${ccd.server.url}")
    private String serverURL;

    @Value("${ccd.app.title}")
    private String title;

    @Value("${ccd.app.copyright}")
    private String copyright;

    @Value("${ccd.app.agreement}")
    private String agreement;

    @Value("${ccd.dir.workspace}")
    private String workspaceDir;

    @Value("${ccd.folder.data}")
    private String dataFolder;

    @Value("${ccd.folder.lib}")
    private String libFolder;

    @Value("${ccd.folder.tmp}")
    private String tmpFolder;

    @Value("${ccd.folder.result}")
    private String resultFolder;

    public CcdProperties() {
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getTitle() {
        return title;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getAgreement() {
        return agreement;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public String getLibFolder() {
        return libFolder;
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public String getResultFolder() {
        return resultFolder;
    }

}
