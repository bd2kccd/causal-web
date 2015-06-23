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

import edu.pitt.dbmi.ccd.db.CCDDatabaseApplication;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 *
 * May 14, 2015 12:36:29 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Configuration
@Import(CCDDatabaseApplication.class)
public class WebConfigurer extends WebMvcConfigurerAdapter {

    @Bean(name = "ccd")
    public PropertiesFactoryBean propertiesFactoryBean() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("/ccd.properties"));

        return bean;
    }

    @Bean
    @ConditionalOnProperty(name = "app.webapp", havingValue = "true")
    public EmbeddedServletContainerCustomizer webappEmbeddedServletContainerCustomizer() {
        return (ConfigurableEmbeddedServletContainer container) -> {
            ErrorPage[] errorPages = {
                new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
                new ErrorPage(HttpStatus.UNAUTHORIZED, "/401")
            };
            container.setContextPath("/ccd");
            container.addErrorPages(errorPages);
            container.setSessionTimeout(30, TimeUnit.MINUTES);
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.webapp", havingValue = "false")
    public EmbeddedServletContainerCustomizer desktopEmbeddedServletContainerCustomizer() {
        return (ConfigurableEmbeddedServletContainer container) -> {
            ErrorPage[] errorPages = {
                new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
                new ErrorPage(HttpStatus.UNAUTHORIZED, "/401")
            };
            container.addErrorPages(errorPages);
            container.setContextPath("/ccd");
            container.setSessionTimeout(-1);
        };
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setExposeContextBeansAsAttributes(true);

        return resolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

}
