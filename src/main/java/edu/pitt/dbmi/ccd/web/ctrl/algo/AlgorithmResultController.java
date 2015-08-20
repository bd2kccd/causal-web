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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.FileInfo;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.service.cloud.CloudDataService;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Aug 7, 2015 12:45:54 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/results")
public class AlgorithmResultController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmResultController.class);

    private final CloudDataService cloudDataService;

    @Autowired(required = true)
    public AlgorithmResultController(CloudDataService cloudDataService) {
        this.cloudDataService = cloudDataService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        List<FileInfo> listing = new LinkedList<>();
        try {
            List<Path> list = FileInfos.listDirectory(Paths.get(appUser.getResultDirectory()), false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<BasicFileInfo> results = FileInfos.listBasicPathInfo(files);
            results.forEach(result -> {
                FileInfo resultFile = new FileInfo();
                resultFile.setCreationDate(FilePrint.fileTimestamp(result.getCreationTime()));
                resultFile.setFileName(result.getFilename());
                resultFile.setSize(FilePrint.humanReadableSize(result.getSize(), true));
                listing.add(resultFile);
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        listing.addAll(cloudDataService.getUserResultFiles(appUser.getUsername()));

        // sort
        FileInfo[] infoArray = listing.toArray(new FileInfo[listing.size()]);
        Arrays.sort(infoArray, Collections.reverseOrder((e1, e2) -> Long.signum(e1.getRawCreationDate() - e2.getRawCreationDate())));

        model.addAttribute("itemList", listing);

        return ALGORITHM_RESULTS_VIEW;
    }

    @RequestMapping(value = "download/cloud", method = RequestMethod.GET)
    public void downloadResultFileFromCloud(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", filename);
        response.setHeader(headerKey, headerValue);

        byte[] data = cloudDataService.downloadFile(appUser.getUsername(), filename);

        try (ReadableByteChannel inputChannel = Channels.newChannel(new ByteArrayInputStream(data));
                WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            while (inputChannel.read(buffer) != -1) {
                // prepare the buffer to be drained
                buffer.flip();
                // write to the channel, may block
                outputChannel.write(buffer);
                // If partial transfer, shift remainder down
                // If buffer is empty, same as doing clear()
                buffer.compact();
            }
            // EOF will leave buffer in fill state
            buffer.flip();
            // make sure the buffer is fully drained.
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to download file '%s'.", filename), exception);
        }
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        ServletContext context = request.getServletContext();

        Path file = Paths.get(appUser.getResultDirectory(), filename);
        String mimeType = context.getMimeType(file.toAbsolutePath().toString());
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        response.setContentType(mimeType);
        try {
            response.setContentLength((int) Files.size(file));
        } catch (IOException exception) {
            LOGGER.error(
                    String.format("Unable to get file '%s' size.", filename),
                    exception);
        }

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", filename);
        response.setHeader(headerKey, headerValue);

        try {
            Files.copy(file, response.getOutputStream());
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to download file '%s'.", filename), exception);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteResultFile(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser) {

        Path file = Paths.get(appUser.getResultDirectory(), filename);
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        return REDIRECT_ALGORITHM_RESULTS;
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String showResultError(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {

        List<String> errors = new LinkedList<>();
        Path file = Paths.get(appUser.getResultDirectory(), filename);
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                errors.add(line);
            }
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        model.addAttribute("errors", errors);

        return ALGORITHM_RESULT_ERROR_VIEW;
    }

    @RequestMapping(value = PLOT, method = RequestMethod.GET)
    public String showPlot(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        Path file = Paths.get(appUser.getResultDirectory(), filename);
        Map<String, String> parameters = new TreeMap<>();
        Pattern equalDelim = Pattern.compile("=");
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            boolean isParamters = false;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();

                if (isParamters) {
                    String[] data = equalDelim.split(line);
                    if (data.length == 2) {
                        parameters.put(data[0].trim(), data[1].trim());
                    } else {
                        break;
                    }
                } else if ("Graph Parameters:".equals(line)) {
                    isParamters = true;
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", filename), exception);
        }

        model.addAttribute("plot", filename);
        model.addAttribute("link", "/algorithm/results/d3graph?file=" + filename);
        model.addAttribute("parameters", parameters);

        return PLOT_VIEW;
    }

    @RequestMapping(value = D3_GRAPH, method = RequestMethod.GET)
    public String showD3Graph(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        Path file = Paths.get(appUser.getResultDirectory(), filename);

        List<Node> links = new LinkedList<>();
        Pattern space = Pattern.compile("\\s+");
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            boolean isData = false;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (isData) {
                    String[] data = space.split(line);
                    if (data.length == 4) {
                        links.add(new Node(data[1], data[3], data[2]));
                    }
                } else if ("Graph Edges:".equals(line)) {
                    isData = true;
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", filename), exception);
        }

        model.addAttribute("data", links);

        return D3_GRAPH_VIEW;
    }

}
