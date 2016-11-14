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
package edu.pitt.dbmi.ccd.web.ctrl.account;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.user.UserRegistration;
import edu.pitt.dbmi.ccd.web.service.account.UserRegistrationService;

import jdk.nashorn.internal.parser.JSONParser;

/**
 *
 * Oct 4, 2016 2:48:15 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@RequestMapping(value = "/user/account/registration")
public class UserRegistrationController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationController.class);


    private final UserRegistrationService userRegistrationService;

    @Autowired
    @Value("${annotations.api.url:}")
    private String annoApiUrl;

    @Autowired
    @Value("${annotations.api.clientId:}")
    private String annoClientId;

    @Autowired
    @Value("${annotations.api.clientSecret:}")
    private String annoClientSecret;

    private String getAnnotationsTokens(UserRegistration userRegistration) throws Exception {
        Base64.Encoder encoder = Base64.getUrlEncoder();

        String tokenEndpoint = annoApiUrl.concat("oauth/token");
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection;
        if (annoApiUrl.startsWith("https")) {
            connection = (HttpsURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setRequestMethod("POST");
        String clientCredentials = annoClientId + ":" + annoClientSecret;
        String credentials = encoder.encodeToString(clientCredentials.getBytes());
        connection.setRequestProperty("Authorization", "Basic " + credentials);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        String parameters = "username=" + userRegistration.getUsername() + "&password=" + userRegistration.getPassword() + "&grant_type=password";
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject json = new JSONObject(response.toString());
        String accessToken = json.getString("access_token");
        String refreshToken = json.getString("refresh_token");

        return accessToken.concat("&" + refreshToken);
    }

    @Autowired
    public UserRegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String registerNewRegularUser(
            @Valid @ModelAttribute("userRegistration") final UserRegistration userRegistration,
            final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes,
            final Model model,
            final HttpServletRequest req,
            final HttpServletResponse res) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistration", bindingResult);
            redirectAttributes.addFlashAttribute("userRegistration", userRegistration);
            redirectAttributes.addFlashAttribute("errorMsg", "Registration failed!");
        } else {
            userRegistrationService.registerNewRegularUser(userRegistration, false, model, redirectAttributes, req, res);
            try {
                String[] tokens = getAnnotationsTokens(userRegistration).split("&");
                String accessToken = tokens[0];
                String refreshToken = tokens[1];
                Cookie accessCookie = new Cookie("access_token", accessToken);
                accessCookie.setPath("/ccd");
                Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
                refreshCookie.setPath("/ccd");
                res.addCookie(accessCookie);
                res.addCookie(refreshCookie);
            } catch (Exception e) {
                LOGGER.error("Unable to fetch annotations tokens", e);
            }
        }

        return REDIRECT_LOGIN;
    }

    @RequestMapping(value = "activate", method = RequestMethod.GET)
    public String activateNewUser(
            @RequestParam(value = "activation", required = true) final String activation,
            final HttpServletRequest request,
            final RedirectAttributes redirectAttributes) {
        String accountId = new String(Base64.getUrlDecoder().decode(activation));
        userRegistrationService.activateNewUser(accountId, request, redirectAttributes);
        return REDIRECT_MESSAGE;
    }

}
