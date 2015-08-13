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
package edu.pitt.dbmi.ccd.web.conf;

import edu.pitt.dbmi.ccd.mail.service.SimpleMailService;
import edu.pitt.dbmi.ccd.mail.service.UserMailService;
import edu.pitt.dbmi.ccd.web.mail.BasicWebServiceMail;
import edu.pitt.dbmi.ccd.web.mail.BasicWebServiceUserMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * Aug 5, 2015 9:08:28 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Configuration
public class DesktopConfigurer {

    @Bean
    public Boolean webapp() {
        return Boolean.FALSE;
    }

    @Bean
    public SimpleMailService simpleMailService(
            @Value("${ccd.mail.feedback.uri:http://localhost:9000/ccd-ws/mail/feedback}") String uri,
            @Value("${ccd.mail.feedback.appId:1}") String appId) {
        return new BasicWebServiceMail(uri, appId);
    }

    @Bean
    public UserMailService userMailService() {
        return new BasicWebServiceUserMail();
    }

}
