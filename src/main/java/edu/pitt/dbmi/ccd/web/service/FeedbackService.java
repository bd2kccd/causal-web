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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.web.domain.Feedback;
import edu.pitt.dbmi.ccd.web.service.mail.FeedbackMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Aug 31, 2016 8:18:51 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FeedbackService {

    private final FeedbackMailService feedbackMailService;

    @Autowired
    public FeedbackService(FeedbackMailService feedbackMailService) {
        this.feedbackMailService = feedbackMailService;
    }

    public void sendFeedback(Feedback feedback, RedirectAttributes redirectAttributes) {
        feedbackMailService.sendUserFeedback(feedback.getEmail(), feedback.getFeedbackMsg());
        redirectAttributes.addFlashAttribute("successMsg", "Thank you for your feedback!");
    }

}
