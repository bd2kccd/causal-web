/*
 * Copyright (C) 2016 University of Pittsburgh.
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
package edu.pitt.dbmi.ccd.web.service.algo.result;

import edu.pitt.dbmi.ccd.commons.graph.SimpleGraph;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphComparison;
import edu.pitt.dbmi.ccd.commons.graph.SimpleGraphUtil;
import edu.pitt.dbmi.ccd.db.domain.FileTypeName;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.conf.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.file.CheckboxFiles;
import edu.pitt.dbmi.ccd.web.domain.result.ComparedFile;
import edu.pitt.dbmi.ccd.web.domain.result.ResultComparison;
import edu.pitt.dbmi.ccd.web.domain.result.ResultComparisonData;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 *
 * Aug 19, 2016 5:10:49 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgorithmResultComparisonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmResultComparisonService.class);

    private final CcdProperties ccdProperties;
    private final UserAccountService userAccountService;
    private final FileManagementService fileManagementService;
    private final FileService fileService;

    @Autowired
    public AlgorithmResultComparisonService(CcdProperties ccdProperties, UserAccountService userAccountService, FileManagementService fileManagementService, FileService fileService) {
        this.ccdProperties = ccdProperties;
        this.userAccountService = userAccountService;
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;
    }

    public void showFileInfo(Long id, AppUser appUser, Model model) {
        File file = fileManagementService.retrieveFile(id, appUser);

        Path xmlFile = Paths.get(file.getAbsolutePath(), file.getName());
        ResultComparison resultComparison = readFromFile(xmlFile);
        ensureComparedFileExists(resultComparison, xmlFile);

        model.addAttribute("file", file);
        model.addAttribute("comparedFiles", resultComparison.getComparedFiles());
        model.addAttribute("comparisonData", resultComparison.getComparisonData());
    }

    private void ensureComparedFileExists(ResultComparison resultComparison, Path xmlFile) {

        boolean titleHasChanged = false;
        List<ComparedFile> comparedFiles = resultComparison.getComparedFiles();
        for (ComparedFile comparedFile : comparedFiles) {
            Long id = comparedFile.getId();
            if (id != null) {
                File file = fileService.findById(comparedFile.getId());
                if (file == null) {
                    comparedFile.setId(null);
                } else if (FileTypeName.valueOf(file.getFileType().getName()) != FileTypeName.ALGORITHM_RESULT) {
                    comparedFile.setId(null);
                } else if (!file.getMd5checkSum().equals(comparedFile.getMd5checkSum())) {
                    comparedFile.setId(null);
                } else {
                    String title = file.getTitle();
                    if (title.compareTo(comparedFile.getTitle()) != 0) {
                        comparedFile.setTitle(title);
                        titleHasChanged = true;
                    }
                }
            }
        }

        if (titleHasChanged) {
            writeToFile(resultComparison, xmlFile);
        }
    }

    public void compare(CheckboxFiles checkboxFiles, AppUser appUser) {
        List<Long> ids = checkboxFiles.getFileIds();
        if (ids.isEmpty()) {
            return;
        }
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        List<File> files = fileService.findByIdsAndUserAccount(ids, userAccount);
        SimpleGraphComparison comparison = compareGraph(files);
        ResultComparison resultComparison = createResultComparison(comparison, files);

        String title = "result_comparison_" + System.currentTimeMillis();
        String name = title + ".xml";
        String workspace = userAccount.getPerson().getWorkspace();
        Path xmlFile = Paths.get(workspace, ccdProperties.getDataFolder(), name);
        boolean success = writeToFile(resultComparison, xmlFile);
        if (success) {
            try {
                File file = fileManagementService.createFileEntity(xmlFile, userAccount);
                file.setFileType(fileManagementService.findFileType(FileTypeName.ALGORITHM_RESULT_COMPARISON));
                fileService.save(file);
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
                try {
                    Files.delete(xmlFile);
                } catch (IOException iOException) {
                    LOGGER.error(iOException.getMessage());
                }
            }
        }
    }

    private ResultComparison createResultComparison(SimpleGraphComparison comparison, List<File> files) {
        List<ComparedFile> comparedFiles = new LinkedList<>();
        files.forEach(file -> {
            comparedFiles.add(new ComparedFile(file.getId(), file.getTitle(), file.getMd5checkSum()));
        });

        List<ResultComparisonData> comparisonDataList = new LinkedList<>();
        Set<String> distinctEdges = comparison.getDistinctEdges();
        Set<String> edgesInAll = comparison.getEdgesInAll();
        Set<String> sameEdgeTypes = comparison.getSameEdgeTypes();
        int countIndex = 0;
        for (String edge : distinctEdges) {
            ResultComparisonData comparisonData = new ResultComparisonData();
            comparisonData.setCountIndex(++countIndex);
            comparisonData.setEdge(edge);
            comparisonData.setInAll(edgesInAll.contains(edge));
            comparisonData.setSameEdgeType(sameEdgeTypes.contains(edge));

            comparisonDataList.add(comparisonData);
        }

        return new ResultComparison(comparedFiles, comparisonDataList);
    }

    private SimpleGraphComparison compareGraph(List<File> files) {
        SimpleGraphComparison comparison = new SimpleGraphComparison();

        List<SimpleGraph> graphs = new LinkedList<>();
        files.forEach(file -> {
            Path resultFile = Paths.get(file.getAbsolutePath(), file.getName());
            if (Files.exists(resultFile)) {
                try (BufferedReader reader = Files.newBufferedReader(resultFile, Charset.defaultCharset())) {
                    graphs.add(SimpleGraphUtil.readInSimpleGraph(reader));
                } catch (IOException exception) {
                    LOGGER.error(String.format("Unable to read file '%s'.", file.getName()), exception);
                }
            }
        });

        comparison.compare(graphs);

        return comparison;
    }

    public void listResults(AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        FileType fileType = fileManagementService.findFileType(FileTypeName.ALGORITHM_RESULT_COMPARISON);

        fileManagementService.syncDatabaseWithDirectory(fileType, userAccount);

        model.addAttribute("pageTitle", "Algorithm Result Comparison Files");
        model.addAttribute("files", fileService.findByFileTypeAndUserAccount(fileType, userAccount));
    }

    private boolean writeToFile(ResultComparison resultComparison, Path outFile) {
        boolean success = true;

        try (OutputStream outputStream = Files.newOutputStream(outFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(ResultComparison.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(resultComparison, outputStream);
        } catch (IOException | JAXBException exception) {
            LOGGER.error(exception.getMessage());
            success = false;
        }

        return success;
    }

    private ResultComparison readFromFile(Path xmlFile) {
        ResultComparison resultComparison = null;

        try (BufferedReader reader = Files.newBufferedReader(xmlFile, StandardCharsets.UTF_8)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(ResultComparison.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            resultComparison = (ResultComparison) unmarshaller.unmarshal(reader);
        } catch (IOException | JAXBException exception) {
            LOGGER.error(exception.getMessage());
        }

        return resultComparison;
    }

}
