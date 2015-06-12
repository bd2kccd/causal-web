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
package edu.pitt.dbmi.ccd.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 *
 * Apr 16, 2015 11:41:02 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgorithmService {

    private final String tempDirectory;

    private final String outputDirectory;

    @Autowired(required = true)
    public AlgorithmService(
            @Value(value = "#{app.tempDir}") String tempDirectory,
            @Value(value = "#{app.outputDir}") String outputDirectory) {
        this.tempDirectory = tempDirectory;
        this.outputDirectory = outputDirectory;

        Path outDir = Paths.get(outputDirectory);
        if (Files.notExists(outDir)) {
            try {
                Files.createDirectories(outDir);
            } catch (IOException exception) {
                exception.printStackTrace(System.err);
            }
        }
    }

    @Async
    public Future<Void> runAlgorithm(String cmd, String fileName) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd + " --out " + tempDirectory);
        process.waitFor();

        Path source = Paths.get(tempDirectory, fileName);
        Path target = Paths.get(outputDirectory, fileName);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

        return new AsyncResult<>(null);
    }

}
