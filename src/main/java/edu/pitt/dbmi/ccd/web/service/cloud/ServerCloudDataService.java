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
package edu.pitt.dbmi.ccd.web.service.cloud;

import edu.pitt.dbmi.ccd.web.model.FileInfo;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * Aug 17, 2015 11:12:33 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class ServerCloudDataService implements CloudDataService {

    @Override
    public byte[] downloadFile(String username, String fileName) {
        return null;
    }

    @Override
    public Set<String> getDataMd5Hash(String username) {
        Set<String> hashes = new HashSet<>();

        return hashes;
    }

    @Override
    public List<FileInfo> getUserResultFiles(String username) {
        List<FileInfo> list = new LinkedList<>();

        return list;
    }

}
