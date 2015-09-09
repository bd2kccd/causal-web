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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparisonData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

/**
 *
 * Sep 6, 2015 12:10:33 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractResultFileService {

    protected void downloadLocalResultFile(String fileName, AppUser appUser, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
        response.setHeader(headerKey, headerValue);

        Path file = Paths.get(appUser.getAlgoResultDir(), fileName);
        response.setContentLength((int) Files.size(file));

        Files.copy(file, response.getOutputStream());
    }

    protected List<ResultFileInfo> getUserLocalResultFiles(String directory) throws IOException {
        List<ResultFileInfo> fileInfos = new LinkedList<>();

        List<Path> list = FileInfos.listDirectory(Paths.get(directory), false);
        List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

        List<BasicFileInfo> results = FileInfos.listBasicPathInfo(files);
        results.forEach(result -> {
            String fileName = result.getFilename();

            ResultFileInfo fileInfo = new ResultFileInfo();
            fileInfo.setCreationDate(FilePrint.fileTimestamp(result.getCreationTime()));
            fileInfo.setFileName(fileName);
            fileInfo.setSize(FilePrint.humanReadableSize(result.getSize(), true));
            fileInfo.setRawCreationDate(result.getCreationTime());
            fileInfo.setError(fileName.startsWith("error"));

            fileInfos.add(fileInfo);
        });

        return fileInfos;
    }

    protected void extractNodes(BufferedReader reader, List<Node> nodes) throws IOException {
        Pattern space = Pattern.compile("\\s+");
        boolean isData = false;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (isData) {
                String[] data = space.split(line);
                if (data.length == 4) {
                    nodes.add(new Node(data[1], data[3], data[2]));
                }
            } else if ("Graph Edges:".equals(line)) {
                isData = true;
            }
        }
    }

    protected void extractParameters(BufferedReader reader, Map<String, String> parameters) throws IOException {
        Pattern equalDelim = Pattern.compile("=");
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
    }

    protected void readInErrorMessage(BufferedReader reader, List<String> errorMsg) throws IOException {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            errorMsg.add(line);
        }
    }

    protected void writeResultComparison(BufferedWriter writer, ResultComparison resultComparison) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<String> fileNames = resultComparison.getFileNames();
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
    }

}
