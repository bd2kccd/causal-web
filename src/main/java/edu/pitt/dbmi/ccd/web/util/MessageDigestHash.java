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
package edu.pitt.dbmi.ccd.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * Apr 1, 2015 4:05:06 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class MessageDigestHash {

    private static final String MD5_ALGORITHM = "MD5";

    private MessageDigestHash() {
    }

    /**
     * Compute MD5 hash on a file.
     *
     * @param path
     * @return MD5 hash string of the file
     * @throws IOException
     */
    public static final String computeMD5Hash(Path path) throws IOException {
        String hash = null;
        try {
            byte[] digest = getMessageDigest(path, MD5_ALGORITHM);
            StringBuilder sb = new StringBuilder(digest.length);
            for (byte b : digest) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace(System.err);
        }
        return hash;
    }

    private static byte[] getMessageDigest(Path path, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try (InputStream is = Files.newInputStream(path)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            dis.on(true); // no need to call md.update(buffer) when set ON
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
            }
        }
        return md.digest();
    }

}
