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
package edu.pitt.dbmi.ccd.web.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Future;
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

    public AlgorithmService() {
    }

    @Async
    public Future<Void> runAlgorithm(List<String> commands, String fileName, String tmpDirectory, String outputDirectory) throws Exception {
        commands.add("--out");
        commands.add(tmpDirectory);

        fileName = String.format("%s.txt", fileName);
        String errorFileName = String.format("error_%s", fileName);
        Path error = Paths.get(tmpDirectory, errorFileName);
        Path errorDest = Paths.get(outputDirectory, errorFileName);
        Path src = Paths.get(tmpDirectory, fileName);
        Path dest = Paths.get(outputDirectory, fileName);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectError(error.toFile());
        Process process = pb.start();
        process.waitFor();

        if (process.exitValue() == 0) {
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(error);
        } else {
            Files.deleteIfExists(src);
            Files.move(error, errorDest, StandardCopyOption.REPLACE_EXISTING);
        }

        return new AsyncResult<>(null);
    }

}
