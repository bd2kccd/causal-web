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

import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.model.file.ResultFileInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    final String workspace;

    final String resultFolder;

    final String algorithmResultFolder;

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
        List<Path> filesToDelete = new LinkedList<>();
        fileNames.forEach(fileName -> {
            filesToDelete.add(Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName));
            filesToDelete.add(Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName.replaceAll(".txt$", ".json")));
        });

        filesToDelete.forEach(file -> {
            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                try {
                    Files.delete(file);
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage());
                }
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
                // only accept files that ends in .txt
                if (fileName.endsWith(".txt")) {
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

    public List<Node> extractGraphNodes(final String fileName, final String username) {
        List<Node> nodes = new LinkedList<>();

        String[] edgeTypes = {
            "---", "-->", "<--", "<->", "o->", "<-o", "o-o"
        };

        Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            Pattern space = Pattern.compile("\\s+");
            boolean isData = false;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (isData) {
                    String[] data = space.split(line, 2);
                    if (data.length == 2) {
                        String value = data[1].trim();

                        String edge = "";
                        for (String edgeType : edgeTypes) {
                            if (value.contains(edgeType)) {
                                edge = edgeType;
                                break;
                            }
                        }
                        String[] values = value.split(edge);
                        if (values.length == 2) {
                            String source = values[0].trim();
                            String target = values[1].trim();
                            nodes.add(new Node(source, target, edge));
                        }
                    }
                } else if ("Graph Edges:".equals(line)) {
                    isData = true;
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
        }

        return nodes;
    }

}
