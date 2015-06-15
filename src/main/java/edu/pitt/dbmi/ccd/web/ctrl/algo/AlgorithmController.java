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
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.web.util.FileUtility;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * Apr 7, 2015 9:52:57 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AlgorithmController {

    protected final String uploadDirectory;

    protected final String libDirectory;

    protected final String outputDirectory;

    protected final String tempDirectory;

    protected final String algorithmJar;

    public AlgorithmController(String uploadDirectory, String libDirectory,
            String outputDirectory, String tempDirectory, String algorithmJar) {
        this.uploadDirectory = uploadDirectory;
        this.libDirectory = libDirectory;
        this.outputDirectory = outputDirectory;
        this.tempDirectory = tempDirectory;
        this.algorithmJar = algorithmJar;
    }

    protected Map<String, String> directoryFileListing(Path directory) {
        Map<String, String> map = new TreeMap<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path path : directoryStream) {
                String key = path.getFileName().toString();
                String value = String.format(("%s (%s)"),
                        key,
                        FileUtility.humanReadableSize(Files.size(path), true));
                map.put(key, value);
            }
        } catch (IOException exception) {
        }

        return map;
    }

}
