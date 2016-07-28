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
package edu.pitt.dbmi.ccd.web.service.result.algo.run;

import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.d3.Node;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.prop.CcdAlgoProperties;
import edu.pitt.dbmi.ccd.web.service.file.FileManagementService;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 *
 * Jul 27, 2016 7:20:18 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AlgoRunResultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlgoRunResultService.class);

    private final CcdAlgoProperties ccdAlgoProperties;
    private final UserAccountService userAccountService;
    private final FileManagementService fileManagementService;
    private final FileService fileService;

    private final FileType algoResultFileType;

    @Autowired
    public AlgoRunResultService(CcdAlgoProperties ccdAlgoProperties, UserAccountService userAccountService, FileManagementService fileManagementService, FileService fileService, FileTypeService fileTypeService) {
        this.ccdAlgoProperties = ccdAlgoProperties;
        this.userAccountService = userAccountService;
        this.fileManagementService = fileManagementService;
        this.fileService = fileService;

        this.algoResultFileType = fileTypeService.findByName(FileTypeService.ALGO_RESULT_TYPE_NAME);
    }

    public void listResults(AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        fileManagementService.syncDatabaseWithDirectory(algoResultFileType, userAccount);

        model.addAttribute("pageTitle", "Algorithm Run Result Files");
        model.addAttribute("itemList", fileService.findByFileTypeAndUserAccount(algoResultFileType, userAccount));
    }

    public void showResultInfo(Long id, AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("fileId", file.getId());
        model.addAttribute("fileName", file.getName());

        List<String> categoryNames = Arrays.asList(ccdAlgoProperties.getResultInfoCategories());
        model.addAttribute("categories", extractDataCategories(file, categoryNames));
    }

    public void showD3Graph(Long id, AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("data", extractGraphNodes(file));
    }

    public List<Node> extractGraphNodes(File fileEntity) {
        List<Node> nodes = new LinkedList<>();

        String[] edgeTypes = {
            "---", "-->", "<--", "<->", "o->", "<-o", "o-o"
        };

        Path file = Paths.get(fileEntity.getAbsolutePath(), fileEntity.getName());
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
            LOGGER.error(String.format("Unable to read file '%s'.", fileEntity.getName()), exception);
        }

        return nodes;
    }

    private Map<String, Map<String, String>> extractDataCategories(File fileEntity, final List<String> categoryNames) {
        Map<String, Map<String, String>> info = new LinkedHashMap<>();
        categoryNames.forEach(key -> {
            info.put(key, new LinkedHashMap<>());
        });

        Path file = Paths.get(fileEntity.getAbsolutePath(), fileEntity.getName());
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
            LOGGER.error(String.format("Unable to read file '%s'.", fileEntity.getName()), exception);
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

}
