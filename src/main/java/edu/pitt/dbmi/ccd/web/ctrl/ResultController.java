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

package edu.pitt.dbmi.ccd.web.ctrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.util.FileUtility;

/**
 *
 * May 21, 2015 4:26:11 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 * 
 */
@Controller
@RequestMapping(value = "/results")
public class ResultController implements ViewController {

    private final String outputDirectory;

    @Autowired(required = true)
    public ResultController(@Value("${app.outputDir}") String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(Model model) {
    	model.addAttribute("itemList", FileUtility.getFileListing(outputDirectory));
    	
        return RUNRESULTS;
    }

    @RequestMapping(value = DELETE, method = RequestMethod.GET)
    public String deleteResultFile(@RequestParam(value = "file") String filename, Model model) {
        Path file = Paths.get(outputDirectory, filename);
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        return REDIRECT_RESULTS;
    }

    @RequestMapping(value = D3GRAPH, method = RequestMethod.GET)
    public String showD3Graph(@RequestParam(value = "file") String filename, Model model) {
        Path file = Paths.get(outputDirectory, filename);

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
            exception.printStackTrace(System.err);
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(links);
            model.addAttribute("data", json);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }

        return D3GRAPH;
    }

    @RequestMapping(value = PLOT, method = RequestMethod.GET)
    public String showPlot(@RequestParam(value = "file") String filename, Model model) {
        Path file = Paths.get(outputDirectory, filename);

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
            exception.printStackTrace(System.err);
        }

        model.addAttribute("plot", filename);
        model.addAttribute("link", "d3graph?file=" + filename);
        model.addAttribute("parameters", parameters);

        return PLOT;
    }

    @RequestMapping(value = DOWNLOAD, method = RequestMethod.GET)
    public void downloadResultFile(
            @RequestParam(value = "file") String filename,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        ServletContext context = request.getServletContext();

        Path file = Paths.get(outputDirectory, filename);

        String mimeType = context.getMimeType(file.toAbsolutePath().toString());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setContentLength((int) Files.size(file));

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", filename);
        response.setHeader(headerKey, headerValue);

        Files.copy(file, response.getOutputStream());
    }

}
