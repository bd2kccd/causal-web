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
package edu.pitt.dbmi.ccd.web.util;

import java.util.Random;

/**
 *
 * Jun 29, 2016 4:04:22 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PasswordTool {

    private PasswordTool() {
    }

    public static String generatePassword(int length) {
        StringBuilder sb = new StringBuilder("ccd");

        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<>?/;:/*-+.#$%^&£!";
        int bound = alphabet.length();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(bound)));
        }

        return sb.toString();
    }

}
