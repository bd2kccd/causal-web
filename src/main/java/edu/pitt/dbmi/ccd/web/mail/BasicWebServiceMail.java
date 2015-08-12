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
package edu.pitt.dbmi.ccd.web.mail;

import edu.pitt.dbmi.ccd.mail.service.SimpleMailService;
import javax.mail.MessagingException;

/**
 *
 * Aug 12, 2015 11:32:23 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class BasicWebServiceMail implements SimpleMailService {

    @Override
    public void send(String to, String subject, String body, boolean html) throws MessagingException {
        System.out.println("================================================================================");
        System.out.printf("To: %s\n", to);
        System.out.printf("Subject: %s\n", subject);
        System.out.printf("HTML: %s\n", html);
        System.out.println("================================================================================");

    }

}
