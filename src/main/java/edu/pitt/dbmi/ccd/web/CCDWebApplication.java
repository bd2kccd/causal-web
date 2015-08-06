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
package edu.pitt.dbmi.ccd.web;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CCDWebApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        return applicationBuilder.sources(CCDWebApplication.class);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CCDWebApplication.class, args);
        String[] profiles = context.getEnvironment().getActiveProfiles();
        boolean desktop = false;
        for (String profile : profiles) {
            if ("desktop".equals(profile)) {
                desktop = true;
                break;
            }
        }

        if (desktop) {
            launchBrowser(context.getEnvironment());
        }
    }

    private static void launchBrowser(ConfigurableEnvironment environment) {
        String port = environment.getProperty("server.port", "8080");
        String address = environment.getProperty("server.address", "localhost");
        String url = String.format("http://%s:%s/ccd", address, port);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException exception) {
                exception.printStackTrace(System.err);
            }
        } else {
            /*Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
            }*/
        	String os = System.getProperty("os.name");
            System.getProperties().list(System.out);
            
            List<String> commands = new LinkedList<>();
            try {
                if(os.contains("Mac")){
                	String[] env = {"PATH=/bin:/usr/bin/"};
                	commands.add("open");
                	commands.add(url);
                	Runtime.getRuntime().exec(StringUtils.join(commands.toArray(), " "), env);
                }else if(os.contains("Windows")){
                	commands.add("cmd");
                	commands.add("/c");
                	commands.add("start");
                	commands.add(url);
                	Runtime.getRuntime().exec(StringUtils.join(commands.toArray(), " "));
                }else{//Unix/Linux/FreeBSD
                	commands.add("xdg-open");
                	commands.add(url);
                	Runtime.getRuntime().exec(StringUtils.join(commands.toArray(), " "));
                }
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
    }

}
