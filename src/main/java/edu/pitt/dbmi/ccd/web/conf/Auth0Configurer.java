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
package edu.pitt.dbmi.ccd.web.conf;

import com.auth0.Auth0Filter;
import com.auth0.Auth0ServletCallback;
import edu.pitt.dbmi.ccd.web.prop.Auth0Properties;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * Feb 17, 2016 12:15:28 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Configuration
@Profile("auth0")
public class Auth0Configurer {

    @Autowired
    private Auth0Properties auth0Properties;

    @Bean
    public Boolean authO() {
        return Boolean.TRUE;
    }

    @Bean(name = "authFilter")
    public FilterRegistrationBean authFilter() {
        System.out.println(auth0Properties);
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new Auth0Filter());
        registrationBean.addInitParameter("auth0.redirect_on_authentication_error", "/login");
        registrationBean.addUrlPatterns("/");

        return registrationBean;
    }

    @Bean(name = "redirectCallback")
    public ServletRegistrationBean redirectCallback() {
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("auth0.redirect_on_success", "/auth0");
        initParameters.put("auth0.redirect_on_error", "/login");
        initParameters.put("auth0.client_id", auth0Properties.getClientId());
        initParameters.put("auth0.client_secret", auth0Properties.getClientSecret());
        initParameters.put("auth0.domain", auth0Properties.getDomain());

        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(new Auth0ServletCallback());
        registrationBean.setInitParameters(initParameters);
        registrationBean.addUrlMappings("/callback");

        return registrationBean;
    }

}
