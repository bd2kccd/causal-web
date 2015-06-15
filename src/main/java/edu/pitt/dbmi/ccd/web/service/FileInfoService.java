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

import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.repository.FileInfoRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * May 26, 2015 2:28:09 PM
 *
 * @author Chirayu (Kong) Wongchokprasitti (chw20@pitt.edu)
 *
 */
@Service
@Transactional
public class FileInfoService {

    private final FileInfoRepository fileInfoRepository;

    /**
     * @param fileInfoRepository
     */
    @Autowired(required = true)
    public FileInfoService(FileInfoRepository fileInfoRepository) {
        super();
        this.fileInfoRepository = fileInfoRepository;
    }

    public List<DataFile> findByFileName(String fileName) {
        return fileInfoRepository.findByFileName(fileName);
    }

    public DataFile findByFileAbsolutePath(String fileAbsolutePath) {
        return fileInfoRepository.findByFileAbsolutePath(fileAbsolutePath);
    }

    public DataFile saveFile(DataFile fileInfoDB) {
        String fileAbsolutePath = fileInfoDB.getFileAbsolutePath();
        DataFile fInfo = findByFileAbsolutePath(fileAbsolutePath);
        if (fInfo != null) {// if a file already exists in DB, update it
            fileInfoDB.setId(fInfo.getId());
        }

        return fileInfoRepository.save(fileInfoDB);
    }

    public void deleteFile(String fileAbsolutePath) {
        DataFile fInfo = findByFileAbsolutePath(fileAbsolutePath);
        if (fInfo != null) {// File already exists in DB
            fileInfoRepository.delete(fInfo);
        }
    }

}
