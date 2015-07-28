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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class CCDWebApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        return applicationBuilder.sources(CCDWebApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CCDWebApplication.class, args);

        String os = System.getProperty("os.name");
        System.getProperties().list(System.out);
        
        List<String> commands = new LinkedList<>();
        try {
            if(os.contains("Mac")){
            	String[] env = {"PATH=/bin:/usr/bin/"};
            	commands.add("open");
            	commands.add("http://localhost:8080/ccd");
            	Runtime.getRuntime().exec(StringUtils.join(commands.toArray(), " "), env);
            }else if(os.contains("Windows")){
            	commands.add("cmd");
            	commands.add("/c");
            	commands.add("start");
            	commands.add("http://localhost:8080/ccd");
            	Runtime.getRuntime().exec(StringUtils.join(commands.toArray(), " "));
            }else{//Unix/Linux
            	commands.add("xdg-open");
            	commands.add("http://localhost:8080/ccd");
            	Runtime.getRuntime().exec(StringUtils.join(commands.toArray(), " "));
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
