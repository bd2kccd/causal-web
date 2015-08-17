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
package edu.pitt.dbmi.ccd.web.ctrl;

import edu.pitt.dbmi.ccd.web.model.Feedback;
import edu.pitt.dbmi.ccd.web.service.mail.MailService;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Aug 12, 2015 8:16:58 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "feedback")
public class FeedbackController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackController.class);

    private final MailService mailService;

    @Autowired(required = true)
    public FeedbackController(MailService mailService) {
        this.mailService = mailService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showFeedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());

        return "feedback";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String greetingSubmit(@ModelAttribute Feedback feedback, Model model) {
        model.addAttribute("feedback", feedback);
        model.addAttribute("successMsg", "Thank you for your feedback!");

        Thread t = new Thread(() -> {
            try {
                mailService.sendFeedback(feedback.getEmail(), feedback.getFeedbackMsg());
            } catch (MessagingException exception) {
                LOGGER.error(exception.getMessage());
            }
        });
        t.start();

        return "feedback";
    }

}
