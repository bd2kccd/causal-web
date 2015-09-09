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
package edu.pitt.dbmi.ccd.web.service.result;

import edu.pitt.dbmi.ccd.commons.graph.SimpleGraph;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphUtil;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 5, 2015 8:40:22 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("server")
@Service
public class ServerResultFileService extends AbstractResultFileService implements ResultFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResultFileService.class);

    @Override
    public List<ResultFileInfo> getUserResultFiles(AppUser appUser) {
        List<ResultFileInfo> results = new LinkedList<>();
        try {
            List<ResultFileInfo> fileInfos = getUserLocalResultFiles(appUser.getAlgoResultDir());
            ResultFileInfo[] array = fileInfos.toArray(new ResultFileInfo[fileInfos.size()]);

            Arrays.sort(array, Collections.reverseOrder());  // sort

            results.addAll(Arrays.asList(array));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return results;
    }

    @Override
    public void downloadResultFile(String fileName, boolean remote, AppUser appUser, HttpServletRequest request, HttpServletResponse response) {
        if (!remote) {
            try {
                downloadLocalResultFile(fileName, appUser, request, response);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to download file '%s'.", fileName), exception);
            }
        }
    }

    @Override
    public Map<String, String> getPlotParameters(String fileName, boolean remote, AppUser appUser) {
        Map<String, String> parameters = new TreeMap<>();

        if (!remote) {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                extractParameters(reader, parameters);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return parameters;
    }

    @Override
    public List<Node> getGraphNodes(String fileName, boolean remote, AppUser appUser) {
        List<Node> nodes = new LinkedList<>();

        if (!remote) {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                extractNodes(reader, nodes);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return nodes;
    }

    @Override
    public List<String> getErrorMessages(String fileName, boolean remote, AppUser appUser) {
        List<String> errorMsg = new LinkedList<>();

        if (!remote) {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                readInErrorMessage(reader, errorMsg);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
            }
        }

        return errorMsg;
    }

    @Override
    public void deleteResultFile(List<String> fileNames, AppUser appUser) {
        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        });
    }

    @Override
    public List<SimpleGraph> compareResultFile(List<String> fileNames, AppUser appUser) {
        List<SimpleGraph> graphs = new LinkedList<>();

        fileNames.forEach(fileName -> {
            Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
            if (Files.exists(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                    graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            }
        });

        return graphs;
    }

    @Override
    public void writeResultComparison(ResultComparison resultComparison, String fileNameOut, AppUser appUser) {
        Path file = Paths.get(appUser.getResultComparisonDir(), fileNameOut);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
            writeResultComparison(writer, resultComparison);
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to write file '%s'.", fileNameOut), exception);
        }
    }

}
