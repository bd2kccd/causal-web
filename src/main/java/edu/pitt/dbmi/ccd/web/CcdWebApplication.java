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

import edu.pitt.dbmi.ccd.db.CCDDatabaseApplication;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 *
 * Aug 5, 2015 1:27:02 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@SpringBootApplication
@EnableAsync
@Import(CCDDatabaseApplication.class)
@PropertySource("classpath:ccd.properties")
public class CcdWebApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CcdWebApplication.class);
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
            String os = System.getProperty("os.name");
//            System.getProperties().list(System.out);

            List<String> commands = new LinkedList<>();
            ProcessBuilder pb = new ProcessBuilder(commands);
            if (os.contains("Mac")) {
                commands.add("open");
                commands.add(url);
                Map<String, String> env = pb.environment();
                String key = "PATH";
                String path = env.get(key);
                if (path == null) {
                    path = "/bin:/usr/bin";
                } else {
                    if (!path.contains("/bin")) {
                        path += ":/bin";
                    }
                    if (!path.contains("/usr/bin")) {
                        path += ":/usr/bin";
                    }
                }
                env.put(key, path);
            } else if (os.contains("Windows")) {
                commands.add("cmd");
                commands.add("/c");
                commands.add("start");
                commands.add(url);
            } else if (os.contains("Linux")) {
                commands.add("xdg-open");
                commands.add(url);
            } else {
                System.err.printf("Operating System not supported (%s).  Unable to launch browser.", os);
            }
            try {
                pb.start();
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CcdWebApplication.class, args);
        String[] profiles = context.getEnvironment().getActiveProfiles();
        boolean isDesktopApp = false;
        for (String profile : profiles) {
            if ("desktop".equals(profile)) {
                isDesktopApp = true;
                break;
            }
        }

        if (isDesktopApp) {
            launchBrowser(context.getEnvironment());
        }
    }

}
