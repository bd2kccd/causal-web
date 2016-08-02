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
package edu.pitt.dbmi.ccd.web.service.file;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.MessageDigestHash;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileType;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.entity.VariableFile;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.FileTypeService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.db.service.VariableFileService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.domain.AttributeValue;
import edu.pitt.dbmi.ccd.web.domain.SummaryCount;
import edu.pitt.dbmi.ccd.web.domain.file.CategorizeFile;
import edu.pitt.dbmi.ccd.web.exception.ResourceNotFoundException;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * Jul 8, 2016 11:36:05 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagementService.class);

    private static final String ADDITIONAL_INFO = "additionalInfo";

    private final CcdProperties ccdProperties;
    private final UserAccountService userAccountService;
    private final FileService fileService;
    private final DataFileService dataFileService;
    private final VariableFileService variableFileService;
    private final CategorizeFileService categorizeFileService;

    private final Map<String, FileType> fileTypeMap = new HashMap<>();

    @Autowired
    public FileManagementService(CcdProperties ccdProperties, UserAccountService userAccountService, FileService fileService, DataFileService dataFileService, VariableFileService variableFileService, CategorizeFileService categorizeFileService, FileTypeService fileTypeService) {
        this.ccdProperties = ccdProperties;
        this.userAccountService = userAccountService;
        this.fileService = fileService;
        this.dataFileService = dataFileService;
        this.variableFileService = variableFileService;
        this.categorizeFileService = categorizeFileService;

        List<FileType> fileTypes = fileTypeService.findAll();
        fileTypes.forEach(fileType -> {
            fileTypeMap.put(fileType.getName(), fileType);
        });
    }

    public FileType getFileType(String fileTypeName) {
        return fileTypeMap.get(fileTypeName);
    }

    public void showSummaryCounts(AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }

        syncDatabaseWithDirectory(userAccount);

        Long newUploadCounts = fileService.countUntypedFileByUserAccount(userAccount);
        Long dataCounts = fileService.countByFileTypeAndUserAccount(fileTypeMap.get(FileTypeService.DATA_TYPE_NAME), userAccount);
        Long varCounts = fileService.countByFileTypeAndUserAccount(fileTypeMap.get(FileTypeService.VAR_TYPE_NAME), userAccount);
        Long priorCounts = fileService.countByFileTypeAndUserAccount(fileTypeMap.get(FileTypeService.PRIOR_TYPE_NAME), userAccount);

        List<SummaryCount> summaryCounts = new LinkedList<>();
        summaryCounts.add(new SummaryCount("New Uploads", newUploadCounts, "panel panel-primary", "fa fa-file fa-5x white", ViewPath.NEW_UPLOAD));
        summaryCounts.add(new SummaryCount("Data", dataCounts, "panel panel-green", "fa fa-file fa-5x white", ViewPath.DATA_INPUT));
        summaryCounts.add(new SummaryCount("Prior Knowledge", priorCounts, "panel panel-green", "fa fa-file fa-5x white", ViewPath.PRIOR_KNOWLEDGE_INPUT));
        summaryCounts.add(new SummaryCount("Variable", varCounts, "panel panel-green", "fa fa-file fa-5x white", ViewPath.VARIABLE_INPUT));

        model.addAttribute("summaryCounts", summaryCounts);
    }

    public void showFileInfo(Long id, AppUser appUser, Model model) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("fileId", id);
        model.addAttribute("fileName", file.getName());
        model.addAttribute("basicInfo", getFileBasicInfo(file));
        model.addAttribute("collapse", Boolean.TRUE);

        Boolean collapse = Boolean.TRUE;
        FileType fileType = file.getFileType();
        String fileTypeName = (fileType == null) ? "" : fileType.getName();
        switch (fileTypeName) {
            case "":
                collapse = Boolean.FALSE;
                break;
            case FileTypeService.DATA_TYPE_NAME:
                model.addAttribute(ADDITIONAL_INFO, getDataFileAdditionalInfo(file));
                break;
            case FileTypeService.PRIOR_TYPE_NAME:
                model.addAttribute(ADDITIONAL_INFO, getPriorFileAdditionalInfo(file));
                break;
            case FileTypeService.VAR_TYPE_NAME:
                model.addAttribute(ADDITIONAL_INFO, getVariableFileAdditionalInfo(file));
                break;
        }
        model.addAttribute("collapse", collapse);

        categorizeFileService.showFileCategorizationOptions(file, model);
    }

    private List<AttributeValue> getPriorFileAdditionalInfo(File file) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("forbiddirect", 0);
        counts.put("requiredirect", 0);
        counts.put("addtemporal", 0);

        Path priorFile = Paths.get(file.getAbsolutePath(), file.getName());
        try (BufferedReader reader = Files.newBufferedReader(priorFile, Charset.defaultCharset())) {
            int count = 0;
            String category = null;
            boolean isKnowledge = false;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (isKnowledge) {
                    if (counts.containsKey(line)) {
                        if (category != null && count > 0) {
                            counts.put(category, count);
                        }
                        category = line;
                        count = 0;
                    } else {
                        count++;
                    }
                } else if ("/knowledge".equalsIgnoreCase(line)) {
                    isKnowledge = true;
                }
            }
            if (category != null && count > 0) {
                counts.put(category, count);
            }
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        List<AttributeValue> infos = new LinkedList<>();
        infos.add(new AttributeValue("Forbid Direct:", counts.get("forbiddirect").toString()));
        infos.add(new AttributeValue("Require Direct:", counts.get("requiredirect").toString()));
        infos.add(new AttributeValue("Add Temporal:", counts.get("addtemporal").toString()));

        return infos;
    }

    private List<AttributeValue> getVariableFileAdditionalInfo(File file) {
        List<AttributeValue> infos = new LinkedList<>();

        VariableFile variableFile = variableFileService.findByFile(file);
        infos.add(new AttributeValue("Number of variables:", variableFile.getNumOfVars().toString()));

        return infos;
    }

    private List<AttributeValue> getDataFileAdditionalInfo(File file) {
        List<AttributeValue> infos = new LinkedList<>();

        DataFile dataFile = dataFileService.findByFile(file);
        infos.add(new AttributeValue("Delimiter:", dataFile.getFileDelimiter().getName()));
        infos.add(new AttributeValue("Variable:", dataFile.getVariableType().getName()));
        infos.add(new AttributeValue("Number of Columns:", dataFile.getNumOfColumns().toString()));
        infos.add(new AttributeValue("Number of Rows:", dataFile.getNumOfRows().toString()));

        return infos;
    }

    public boolean categorizeFile(Long id, CategorizeFile categorizeFile, AppUser appUser, RedirectAttributes redirectAttributes) {
        boolean success = categorizeFileService.categorizeFile(id, appUser, categorizeFile);
        if (!success) {
            redirectAttributes.addFlashAttribute("collapse", Boolean.FALSE);
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to categorize file.");
        }

        return success;
    }

    public FileType getFileType(Long id, AppUser appUser) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        return file.getFileType();
    }

    public File deleteFile(Long id, AppUser appUser) {
        UserAccount userAccount = userAccountService.findByUsername(appUser.getUsername());
        if (userAccount == null) {
            throw new ResourceNotFoundException();
        }
        File file = fileService.findByIdAndUserAccount(id, userAccount);
        if (file == null) {
            throw new ResourceNotFoundException();
        }

        try {
            fileService.delete(file);
            Files.deleteIfExists(Paths.get(file.getAbsolutePath(), file.getName()));
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return file;
    }

    public List<AttributeValue> getFileBasicInfo(File fileEntity) {
        List<AttributeValue> basicInfos = new LinkedList<>();

        Path localFile = Paths.get(fileEntity.getAbsolutePath(), fileEntity.getName());
        try {
            BasicFileInfo info = FileInfos.basicPathInfo(localFile);
            basicInfos.add(new AttributeValue("Size:", FilePrint.humanReadableSize(info.getSize(), true)));
            basicInfos.add(new AttributeValue("Creation Time:", FilePrint.fileTimestamp(info.getCreationTime())));
            basicInfos.add(new AttributeValue("Last Access Time:", FilePrint.fileTimestamp(info.getLastAccessTime())));
            basicInfos.add(new AttributeValue("Last Modified Time:", FilePrint.fileTimestamp(info.getLastModifiedTime())));
            basicInfos.add(new AttributeValue("MD5:", fileEntity.getMd5checkSum()));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return basicInfos;
    }

    public void syncDatabaseWithDirectory(UserAccount userAccount) {
        String workspace = userAccount.getPerson().getWorkspace();
        String dataFolder = ccdProperties.getDataFolder();
        Path fileDir = Paths.get(workspace, dataFolder);

        syncDatabaseWithDirectory(fileDir, userAccount);
    }

    public void syncDatabaseWithDirectory(Path fileDir, UserAccount userAccount) {
        if (fileDir == null || userAccount == null) {
            return;
        }

        // grab all the files from the database
        Map<String, File> dbFiles = new HashMap<>();
        List<File> filesFromDatabase = fileService.findByUserAccount(userAccount);
        filesFromDatabase.forEach(file -> {
            dbFiles.put(file.getName(), file);
        });

        List<File> filesToSave = new LinkedList<>();
        try {
            List<Path> localFiles = FileInfos.listDirectory(fileDir, false);
            localFiles.forEach(localFile -> {
                String fileName = localFile.getFileName().toString();

                if (dbFiles.containsKey(fileName)) {
                    dbFiles.remove(fileName);
                } else {
                    try {
                        filesToSave.add(createFileEntity(localFile, userAccount));
                    } catch (IOException exception) {
                        LOGGER.error(exception.getMessage());
                    }
                }
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        if (!dbFiles.isEmpty()) {
            List<File> files = new LinkedList<>();
            Set<String> fileNames = dbFiles.keySet();
            fileNames.forEach(fileName -> {
                files.add(dbFiles.get(fileName));
            });
            fileService.delete(files);
        }

        if (!filesToSave.isEmpty()) {
            fileService.save(filesToSave);
        }
    }

    public void syncDatabaseWithDirectory(String fileTypeName, UserAccount userAccount) {
        syncDatabaseWithDirectory(fileTypeMap.get(fileTypeName), userAccount);
    }

    public void syncDatabaseWithDirectory(FileType fileType, UserAccount userAccount) {
        if (fileType == null || userAccount == null) {
            return;
        }

        String workspace = userAccount.getPerson().getWorkspace();
        String dataFolder = ccdProperties.getDataFolder();
        Path fileDir = Paths.get(workspace, dataFolder);

        // grab all the files from the database
        Map<String, File> dbFiles = new HashMap<>();
        List<File> filesFromDatabase = fileService.findByFileTypeAndUserAccount(fileType, userAccount);
        filesFromDatabase.forEach(file -> {
            dbFiles.put(file.getName(), file);
        });

        try {
            List<Path> localFiles = FileInfos.listDirectory(fileDir, false);
            localFiles.forEach(localFile -> {
                String fileName = localFile.getFileName().toString();

                // remove files that are found on the server
                if (dbFiles.containsKey(fileName)) {
                    dbFiles.remove(fileName);
                }
            });
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        // delete all the files in the database that's not found on the server
        if (!dbFiles.isEmpty()) {
            List<File> files = new LinkedList<>();
            Set<String> fileNames = dbFiles.keySet();
            fileNames.forEach(fileName -> {
                files.add(dbFiles.get(fileName));
            });
            fileService.delete(files);
        }
    }

    public File createFileEntity(Path file, UserAccount userAccount) throws IOException {
        BasicFileInfo fileInfo = FileInfos.basicPathInfo(file);
        String name = fileInfo.getFilename();
        String absolutePath = fileInfo.getAbsolutePath().toString();
        String md5checkSum = MessageDigestHash.computeMD5Hash(file);
        Date creationTime = new Date(fileInfo.getCreationTime());
        long fileSize = fileInfo.getSize();

        File fileEntity = new File();
        fileEntity.setAbsolutePath(absolutePath);
        fileEntity.setCreationTime(creationTime);
        fileEntity.setFileSize(fileSize);
        fileEntity.setMd5checkSum(md5checkSum);
        fileEntity.setName(name);
        fileEntity.setUserAccount(userAccount);

        return fileEntity;
    }

}
