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
package edu.pitt.dbmi.ccd.web.service.algo;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edge.Property;
import edu.cmu.tetrad.graph.EdgeTypeProbability;
import edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.util.JsonUtils;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.model.file.ResultFileInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 *
 * Nov 13, 2015 8:37:12 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgorithmResultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmResultService.class);

    private final String workspace;
    private final String resultFolder;
    private final String algorithmResultFolder;

    @Autowired
    public AlgorithmResultService(
            @Value("${ccd.server.workspace}") String workspace,
            @Value("${ccd.folder.results:results}") String resultFolder,
            @Value("${ccd.folder.results.algorithm:algorithm}") String algorithmResultFolder) {
        this.workspace = workspace;
        this.resultFolder = resultFolder;
        this.algorithmResultFolder = algorithmResultFolder;
    }

    public List<String> getErrorMessages(String fileName, String username) {
        List<String> errorMsg = new LinkedList<>();

        Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                errorMsg.add(line);
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
        }

        return errorMsg;
    }

    public void downloadResultFile(String fileName, String username, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);
        try {
            response.setContentLength((int) Files.size(file));
            Files.copy(file, response.getOutputStream());
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void deleteResultFiles(List<String> fileNames, String username) {
        Path dir = Paths.get(workspace, username, resultFolder, algorithmResultFolder);
        fileNames.forEach(e -> {
            try {
                Files.list(dir)
                        .filter(f -> f.getFileName().toString().startsWith(e.replaceAll(".txt$", "")))
                        .forEach(f -> {
                            try {
                                Files.deleteIfExists(f);
                            } catch (IOException exception) {
                                String errMsg = String.format("Unable to delete result file %s.", e);
                                LOGGER.error(errMsg, exception);
                            }
                        });
            } catch (IOException exception) {
                LOGGER.error("Unable to delete result files.", exception);
            }
        });
    }

    public int countFiles(final String username) {
        int count = 0;

        try {
            Path dir = Paths.get(workspace, username, resultFolder, algorithmResultFolder);
            List<Path> list = FileInfos.listDirectory(dir, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
            count = files.size();
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return count;
    }

    public List<ResultFileInfo> listResultFileInfo(final String username) {
        List<ResultFileInfo> resultFileInfos = new LinkedList<>();
        try {
            Path dir = Paths.get(workspace, username, resultFolder, algorithmResultFolder);
            List<Path> list = FileInfos.listDirectory(dir, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<BasicFileInfo> results = FileInfos.listBasicPathInfo(files);
            results.forEach(result -> {
                String fileName = result.getFilename();
                // only accept files that ends in "_graph.json"
                if (fileName.endsWith("_graph.json")) {
                    ResultFileInfo fileInfo = new ResultFileInfo();
                    fileInfo.setCreationDate(new Date(result.getCreationTime()));
                    fileInfo.setFileName(fileName);
                    fileInfo.setFileSize(result.getSize());
                    fileInfo.setError(fileName.startsWith("error"));

                    resultFileInfos.add(fileInfo);
                }
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        // sort results in descent order
        ResultFileInfo[] array = resultFileInfos.toArray(new ResultFileInfo[resultFileInfos.size()]);
        Arrays.sort(array, (fileInfo1, fileInfo2) -> {
            return fileInfo2.getCreationDate().compareTo(fileInfo1.getCreationDate());
        });

        return Arrays.asList(array);
    }

    public boolean isPagResult(final String fileName, final String username) {
        boolean isPag = false;

        Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);
        try (Scanner scanner = new Scanner(Files.newBufferedReader(file))) {
            scanner.nextLine();  // skip the first line
            String line = scanner.nextLine();
            if (line != null) {
                isPag = line.trim().contains("GFCI");
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
        }

        return isPag;
    }

    public Map<String, Map<String, String>> extractDataCategories(final String fileName, final String username, final List<String> categoryNames) {
        Map<String, Map<String, String>> info = new LinkedHashMap<>();
        categoryNames.forEach(key -> {
            info.put(key, new LinkedHashMap<>());
        });
        Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            Pattern equalDelim = Pattern.compile("=");
            boolean isCategory = false;
            Map<String, String> map = null;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();

                if (isCategory) {
                    if (line.isEmpty()) {
                        isCategory = false;
                    } else if (map != null) {
                        String[] data = equalDelim.split(line);
                        if (data.length == 2) {
                            map.put(data[0].trim(), data[1].trim());
                        }
                    }
                } else if (line.endsWith(":")) {
                    String name = line.replace(":", "");
                    map = info.get(name);
                    isCategory = true;
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
        }

        // clean up
        List<String> trash = new LinkedList<>();
        Set<String> keySet = info.keySet();
        keySet.forEach(key -> {
            Map<String, String> map = info.get(key);
            if (map.isEmpty()) {
                trash.add(key);
            }
        });
        trash.forEach(key -> {
            info.remove(key);
        });

        return info;
    }

    // Generating d3 graph from the Tetrad graph JSON file instead of text result file - Zhou
    public List<Node> extractEdgesFromTetradGraphJson(final String fileName, final String username) {
        List<Node> nodes = new LinkedList<>();

        Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);

        try {
            // Read Tetrad generated json file
            String contents = new String(Files.readAllBytes(file));

            // Parse to Tetrad graph
            Graph tetradGraph = JsonUtils.parseJSONObjectToTetradGraph(contents);
            
            // Extract the edges, this is the tetrad graph edges.
            // We'll convert them into cyto Edge
            Set<Edge> tetradGraphEdges = tetradGraph.getEdges();

            // For each edge determine the types of endpoints to figure out edge type.
            // Basically convert to these '-->', 'o-o', or 'o->' strings
            tetradGraphEdges.stream().forEach((tetradGraphEdge) -> {
                Endpoint endpoint1 = tetradGraphEdge.getEndpoint1();
                Endpoint endpoint2 = tetradGraphEdge.getEndpoint2();

                String endpoint1Str = "";
                if (endpoint1 == Endpoint.TAIL) {
                    endpoint1Str = "-";
                } else if (endpoint1 == Endpoint.ARROW) {
                    endpoint1Str = "<";
                } else if (endpoint1 == Endpoint.CIRCLE) {
                    endpoint1Str = "o";
                }

                String endpoint2Str = "";
                if (endpoint2 == Endpoint.TAIL) {
                    endpoint2Str = "-";
                } else if (endpoint2 == Endpoint.ARROW) {
                    endpoint2Str = ">";
                } else if (endpoint2 == Endpoint.CIRCLE) {
                    endpoint2Str = "o";
                }
                // Produce a string representation of the edge
                String edgeType = String.format("%s-%s", endpoint1Str, endpoint2Str);
                
                // Create node
                Node node = new Node(tetradGraphEdge.getNode1().getName(), tetradGraphEdge.getNode2().getName(), edgeType);

                // Extract the probability of an edge
                List<EdgeTypeProbability> edgeTypeProbabilities = tetradGraphEdge.getEdgeTypeProbabilities();
                
                List<String> edgeTypeProbabilitiesStrings = new LinkedList<>();
                
                HashMap<EdgeType, String> map = new HashMap<>();
                map.put(EdgeType.aa, "<->");
                map.put(EdgeType.ac, "<-o");
                map.put(EdgeType.at, "<--");
                map.put(EdgeType.ca, "o->");
                map.put(EdgeType.cc, "o-o");
                map.put(EdgeType.nil, "no edge");
                map.put(EdgeType.ta, "-->");
                map.put(EdgeType.tt, "---");
                
                edgeTypeProbabilities.forEach(edgeProb -> {
                    edgeTypeProbabilitiesStrings.add(String.format("[%s] %s", map.get(edgeProb.getEdgeType()), edgeProb.getProbability()));
                });
                
                // Set bootstrap edge probabilities
                node.setBootstrap(edgeTypeProbabilitiesStrings);

                // Edge properties
                ArrayList<Property> edgeProperties = tetradGraphEdge.getProperties();
                
                List<String> edgeProps = new LinkedList<>();
                
                edgeProperties.forEach(prob -> {
                    edgeProps.add(String.valueOf(prob));
                });
                        
                if (!edgeProps.isEmpty()) {
                    node.setEdgeProps(edgeProps);
                }

                nodes.add(node);
            });
        } catch (IOException e) {
            LOGGER.error(String.format("Unable to read Tetrad graph JSON file '%s'.", fileName), e);
        }
        
        return nodes;
    }

}
