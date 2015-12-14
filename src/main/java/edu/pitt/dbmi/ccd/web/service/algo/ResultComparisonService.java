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
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraph;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphComparison;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphUtil;
import edu.pitt.dbmi.ccd.web.model.file.FileInfo;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparisonData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
 * Nov 16, 2015 10:59:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class ResultComparisonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultComparisonService.class);

    private static final Pattern TAB_DELIMITER = Pattern.compile("\t");

    final String workspace;

    final String resultFolder;

    final String algorithmResultFolder;

    final String resultComparisonFolder;

    @Autowired
    public ResultComparisonService(
            @Value("${ccd.server.workspace}") String workspace,
            @Value("${ccd.folder.results:results}") String resultFolder,
            @Value("${ccd.folder.results.algorithm:algorithm}") String algorithmResultFolder,
            @Value("${ccd.folder.results.comparison:comparison}") String resultComparisonFolder) {
        this.workspace = workspace;
        this.resultFolder = resultFolder;
        this.algorithmResultFolder = algorithmResultFolder;
        this.resultComparisonFolder = resultComparisonFolder;
    }

    public int countFiles(final String username) {
        int count = 0;

        try {
            Path dir = Paths.get(workspace, username, resultFolder, resultComparisonFolder);
            List<Path> list = FileInfos.listDirectory(dir, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
            count = files.size();
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return count;
    }

    public void compareResultFile(final List<String> fileNames, final String username) {
        if (fileNames.isEmpty()) {
            return;
        }

        List<SimpleGraph> graphs = new LinkedList<>();
        fileNames.forEach(fileName -> {
            Path file = Paths.get(workspace, username, resultFolder, algorithmResultFolder, fileName);
            if (Files.exists(file)) {
                try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                    graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
                }
            }
        });

        SimpleGraphComparison simpleGraphComparison = new SimpleGraphComparison();
        simpleGraphComparison.compare(graphs);

        Set<String> distinctEdges = simpleGraphComparison.getDistinctEdges();
        Set<String> edgesInAll = simpleGraphComparison.getEdgesInAll();
        Set<String> sameEndPoints = simpleGraphComparison.getSameEndPoints();

        String resultFileName = "result_comparison_" + System.currentTimeMillis() + ".txt";

        ResultComparison resultComparison = new ResultComparison(resultFileName);
        resultComparison.getFileNames().addAll(fileNames);

        List<ResultComparisonData> comparisonResults = resultComparison.getComparisonData();
        int countIndex = 0;
        for (String edge : distinctEdges) {
            ResultComparisonData rc = new ResultComparisonData(edge);
            rc.setInAll(edgesInAll.contains(edge));
            rc.setSimilarEndPoint(sameEndPoints.contains(edge));
            rc.setCountIndex(++countIndex);

            comparisonResults.add(rc);
        }

        Path file = Paths.get(workspace, username, resultFolder, resultComparisonFolder, resultFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
            StringBuilder sb = new StringBuilder();
            fileNames.forEach(fileName -> {
                sb.append(fileName);
                sb.append("\t");
            });
            writer.write(sb.toString().trim());
            writer.write("\n");

            List<ResultComparisonData> comparisonData = resultComparison.getComparisonData();
            for (ResultComparisonData comparison : comparisonData) {
                writer.write(String.format("%s\t%s\t%s\n",
                        comparison.getEdge(),
                        comparison.isInAll() ? "1" : "0",
                        comparison.isSimilarEndPoint() ? "1" : "0"));
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to write file '%s'.", resultFileName), exception);
        }
    }

    public List<FileInfo> list(final String username) {
        List<FileInfo> fileInfos = new LinkedList<>();
        try {
            Path resultComparisonDir = Paths.get(workspace, username, resultFolder, resultComparisonFolder);
            List<Path> list = FileInfos.listDirectory(resultComparisonDir, false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<BasicFileInfo> results = FileInfos.listBasicPathInfo(files);
            results.forEach(result -> {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setCreationDate(new Date(result.getCreationTime()));
                fileInfo.setFileName(result.getFilename());
                fileInfo.setFileSize(result.getSize());
                fileInfos.add(fileInfo);
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        // sort results in descent order
        FileInfo[] array = fileInfos.toArray(new FileInfo[fileInfos.size()]);
        Arrays.sort(array, (fileInfo1, fileInfo2) -> {
            return fileInfo2.getCreationDate().compareTo(fileInfo1.getCreationDate());
        });

        return Arrays.asList(array);
    }

    public ResultComparison readInResultComparisonFile(String fileName, String username) {
        ResultComparison resultComparison = new ResultComparison(fileName);

        Path file = Paths.get(workspace, username, resultFolder, resultComparisonFolder, fileName);
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
            String line = reader.readLine();
            if (line != null) {
                List<String> fileNames = resultComparison.getFileNames();
                String[] fields = TAB_DELIMITER.split(line.trim());
                fileNames.addAll(Arrays.asList(fields));
            }

            List<ResultComparisonData> comparisonData = resultComparison.getComparisonData();
            int indexCount = 0;
            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] fields = TAB_DELIMITER.split(line.trim());
                if (fields.length == 3) {
                    ResultComparisonData data = new ResultComparisonData(fields[0]);
                    data.setInAll("1".equals(fields[1]));
                    data.setSimilarEndPoint("1".equals(fields[2]));
                    data.setCountIndex(++indexCount);

                    comparisonData.add(data);
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
        }

        return resultComparison;
    }

    public void download(String fileName, String username, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        try {
            Path file = Paths.get(workspace, username, resultFolder, resultComparisonFolder, fileName);
            if (Files.exists(file)) {
                response.setContentLength((int) Files.size(file));

                Files.copy(file, response.getOutputStream());
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", fileName), exception);
        }
    }

    public void delete(final List<String> fileNames, final String username) {
        fileNames.forEach(fileName -> {
            Path file = Paths.get(workspace, username, resultFolder, resultComparisonFolder, fileName);
            try {
                Files.deleteIfExists(file);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        });
    }

}
