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

import edu.pitt.dbmi.ccd.mail.CCDMailApplication;
import edu.pitt.dbmi.ccd.mail.service.BasicMailService;
import edu.pitt.dbmi.ccd.mail.service.BasicUserMailService;
import edu.pitt.dbmi.ccd.mail.service.SimpleMailService;
import edu.pitt.dbmi.ccd.mail.service.UserMailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 *
 * Aug 5, 2015 2:13:37 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("server")
@Configuration
@Import(CCDMailApplication.class)
public class ServerConfigurer {

    @Bean
    public Boolean webapp() {
        return Boolean.TRUE;
    }

    @Bean
    public SimpleMailService simpleMailService(JavaMailSender javaMailSender) {
        return new BasicMailService(javaMailSender);
    }

    @Bean
    public UserMailService userMailService(SpringTemplateEngine springTemplateEngine, JavaMailSender javaMailSender) {
        return new BasicUserMailService(springTemplateEngine, javaMailSender);
    }

}
