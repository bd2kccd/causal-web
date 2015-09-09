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
package edu.pitt.dbmi.ccd.web.service.result.compare;

import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 8, 2015 11:07:24 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("server")
@Service
public class ServerResultComparisonService extends AbstractResultComparisonService implements ResultComparisonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResultComparisonService.class);

    @Override
    public List<ResultFileInfo> getUserResultComparisonFiles(AppUser appUser) {
        List<ResultFileInfo> results = new LinkedList<>();

        try {
            List<ResultFileInfo> fileInfos = getUserLocalResultComparisonFiles(appUser);
            ResultFileInfo[] array = fileInfos.toArray(new ResultFileInfo[fileInfos.size()]);

            Arrays.sort(array, Collections.reverseOrder());  // sort

            results.addAll(Arrays.asList(array));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return results;
    }

    @Override
    public ResultComparison readInResultComparisonFile(String fileName, boolean remote, AppUser appUser) {
        ResultComparison resultComparison = new ResultComparison(fileName);

        if (!remote) {
            Path file = Paths.get(appUser.getResultComparisonDir(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                extractResultComparison(reader, resultComparison);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return resultComparison;
    }

    @Override
    public void downloadResultComparisonFile(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response) {
        if (!remote) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", fileName);
            response.setHeader(headerKey, headerValue);

            try {
                downloadLocalResultFile(fileName, appUser, request, response);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to download file '%s'.", fileName), exception);
            }
        }
    }

    @Override
    public void deleteResultComparisonFile(List<String> fileNames, AppUser appUser) {
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getResultComparisonDir(), fileName);
            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        });
    }

}
