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
package edu.pitt.dbmi.ccd.web.model;

import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * Aug 12, 2015 8:44:11 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class Feedback {

    private String email;

    @NotEmpty(message = "Feedback message is required.")
    private String feedbackMsg;

    public Feedback() {
    }

    public Feedback(String email, String feedbackMsg) {
        this.email = email;
        this.feedbackMsg = feedbackMsg;
    }

    public String getFeedbackMsg() {
        return feedbackMsg;
    }

    public void setFeedbackMsg(String feedbackMsg) {
        this.feedbackMsg = feedbackMsg;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
