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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparison;
import edu.pitt.dbmi.ccd.web.model.result.ResultComparisonData;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Sep 8, 2015 3:17:06 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractResultComparisonService {

    protected static final Pattern TAB_DELIMITER = Pattern.compile("\t");

    protected void extractResultComparison(BufferedReader reader, ResultComparison resultComparison) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            List<String> fileNames = resultComparison.getFileNames();
            String[] fields = TAB_DELIMITER.split(line.trim());
            for (String field : fields) {
                fileNames.add(field);
            }
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
    }

    protected List<ResultFileInfo> getUserLocalResultComparisonFiles(AppUser appUser) throws IOException {
        List<ResultFileInfo> fileInfos = new LinkedList<>();

        List<Path> list = FileInfos.listDirectory(Paths.get(appUser.getResultComparisonDir()), false);
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

    protected void downloadLocalResultFile(String fileName, AppUser appUser, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path file = Paths.get(appUser.getResultComparisonDir(), fileName);
        if (Files.exists(file)) {
            response.setContentLength((int) Files.size(file));

            Files.copy(file, response.getOutputStream());
        }
    }

}
