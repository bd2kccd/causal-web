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

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.web.model.FileInfo;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Aug 17, 2015 11:12:19 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class DesktopCloudDataService implements CloudDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopCloudDataService.class);

    private final String userDataHashUri;

    private final String userResultsUri;

    private final String userResultFileDownloadUri;

    private final String appId;

    public DesktopCloudDataService(String userDataHashUri, String userResultsUri, String userResultFileDownloadUri, String appId) {
        this.userDataHashUri = userDataHashUri;
        this.userResultsUri = userResultsUri;
        this.userResultFileDownloadUri = userResultFileDownloadUri;
        this.appId = appId;
    }

    @Override
    public byte[] downloadFile(String username, String fileName) {
        String uri = String.format("%s/%s?appId=%s&fileName=%s", userResultFileDownloadUri, username, appId, fileName);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity(uri, ByteArrayResource.class);

        byte[] data = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            ByteArrayResource byteArrayResource = response.getBody();
            data = byteArrayResource.getByteArray();
        }

        return data;
    }

    @Override
    public Set<String> getDataMd5Hash(String username) {
        Set<String> hashes = new HashSet<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            Set<String> set = restTemplate.getForObject(String.format("%s/%s?appId=%s", userDataHashUri, username, appId), Set.class);
            hashes.addAll(set);
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return hashes;
    }

    @Override
    public List<FileInfo> getUserResultFiles(String username) {
        List<FileInfo> list = new LinkedList<>();

        String[] keys = {"fileName", "size", "creationDate"};

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List> entity = restTemplate.getForEntity(String.format("%s/%s?appId=%s", userResultsUri, username, appId), List.class);
            List response = entity.getBody();
            response.forEach(i -> {
                Map map = (Map) i;
                String filename = (String) map.get(keys[0]);
                Integer size = (Integer) map.get(keys[1]);
                Long creationTime = (Long) map.get(keys[2]);

                FileInfo info = new FileInfo(filename, size.toString(), FilePrint.fileTimestamp(creationTime));
                info.setRawCreationDate(creationTime);
                info.setOnCloud(true);
                list.add(info);
            });
        } catch (RestClientException exception) {
            LOGGER.error(exception.getMessage());
        }

        return list;
    }

}
