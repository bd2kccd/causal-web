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
package edu.pitt.dbmi.ccd.web.service;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.repository.DataFileInfoRepository;
import edu.pitt.dbmi.ccd.db.repository.DataFileRepository;
import edu.pitt.dbmi.ccd.web.model.DataListItem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Jul 21, 2015 9:05:34 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
@Transactional
public class DataFileService {

    private final DataFileRepository dataFileRepository;

    private final DataFileInfoRepository dataFileInfoRepository;

    @Autowired(required = true)
    public DataFileService(
            DataFileRepository dataFileRepository,
            DataFileInfoRepository dataFileInfoRepository) {
        this.dataFileRepository = dataFileRepository;
        this.dataFileInfoRepository = dataFileInfoRepository;
    }

    public List<DataListItem> generateListItem(List<Path> files) {
        List<DataListItem> listItems = new LinkedList<>();

        Set<String> fileNames = new HashSet<>();
        List<DataFileInfo> dataFileInfos = dataFileInfoRepository.findAll();
        dataFileInfos.forEach(info -> {
            fileNames.add(info.getDataFile().getName());
        });

        try {
            List<BasicFileInfo> result = FileInfos.listBasicPathInfo(files);
            result.forEach(info -> {
                DataListItem item = new DataListItem();
                item.setCreationDate(FilePrint.fileTimestamp(info.getCreationTime()));
                item.setFileName(info.getFilename());
                item.setSize(FilePrint.humanReadableSize(info.getSize(), true));
                item.setAlert(!fileNames.contains(info.getFilename()));
                listItems.add(item);
            });
            listItems.get(1).setAlert(true);
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        return listItems;
    }

}
