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
package edu.pitt.dbmi.ccd.web.service.mail;

import edu.pitt.dbmi.ccd.mail.AbstractBasicMail;
import edu.pitt.dbmi.ccd.web.conf.prop.CcdEmailProperties;
import edu.pitt.dbmi.ccd.web.util.UriTool;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 *
 * May 26, 2016 1:05:01 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractMailService extends AbstractBasicMail {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMailService.class);

    protected static final Locale LOCALE = new Locale("en", "US");

    protected static final DateFormat EMAIL_DATE_FORMAT = new SimpleDateFormat("EE, MMMMM dd, yyyy hh:mm:ss a");

    protected final SpringTemplateEngine templateEngine;

    protected final CcdEmailProperties ccdEmailProperties;

    public AbstractMailService(SpringTemplateEngine templateEngine, CcdEmailProperties ccdEmailProperties, JavaMailSender javaMailSender) {
        super(javaMailSender);
        this.templateEngine = templateEngine;
        this.ccdEmailProperties = ccdEmailProperties;
    }

    protected void sendMail(Map<String, String> variables, String template, String subject, String... to) throws MessagingException {
        Context context = new Context(LOCALE);
        context.setVariables(variables);

        String body = this.templateEngine.process(template, context);

        send(to, subject, body, true);
    }

    /**
     * Convert numeric value to IP address.
     *
     * @param value - numeric value
     * @return the IP address from a numeric value
     */
    protected String toInetATON(long value) {
        try {
            return UriTool.InetATON(value);
        } catch (UnknownHostException exception) {
            LOGGER.error("Failed to convert to InetATON.", exception);
            return "unknown";
        }
    }

}
