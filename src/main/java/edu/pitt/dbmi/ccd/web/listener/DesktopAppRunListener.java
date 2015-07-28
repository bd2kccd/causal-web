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
package edu.pitt.dbmi.ccd.web.listener;

import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 *
 * Jul 20, 2015 9:38:49 AM
 */
public class DesktopAppRunListener implements SpringApplicationRunListener {

	/* (non-Javadoc)
	 * @see org.springframework.boot.SpringApplicationRunListener#contextLoaded(org.springframework.context.ConfigurableApplicationContext)
	 */
	@Override
	public void contextLoaded(ConfigurableApplicationContext arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.boot.SpringApplicationRunListener#contextPrepared(org.springframework.context.ConfigurableApplicationContext)
	 */
	@Override
	public void contextPrepared(ConfigurableApplicationContext arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.boot.SpringApplicationRunListener#environmentPrepared(org.springframework.core.env.ConfigurableEnvironment)
	 */
	@Override
	public void environmentPrepared(ConfigurableEnvironment arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.boot.SpringApplicationRunListener#finished(org.springframework.context.ConfigurableApplicationContext, java.lang.Throwable)
	 */
	@Override
	public void finished(ConfigurableApplicationContext arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		/*System.out.println("DesktopRunListener is running...");
		try {
			java.awt.Desktop.getDesktop().browse(new URI("http://localhost:8080/ccd"));
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	/* (non-Javadoc)
	 * @see org.springframework.boot.SpringApplicationRunListener#started()
	 */
	@Override
	public void started() {
		// TODO Auto-generated method stub
		
	}

}
