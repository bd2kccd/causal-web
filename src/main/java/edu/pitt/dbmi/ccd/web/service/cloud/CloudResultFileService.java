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
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Aug 21, 2015 9:45:12 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Service
public class CloudResultFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudResultFileService.class);

    private final Boolean webapp;

    private final String appId;

    private final String userResultsUri;

    private final String userResultFileDownloadUri;

    @Autowired(required = true)
    public CloudResultFileService(
            Boolean webapp,
            @Value("${ccd.rest.appId:1}") String appId,
            @Value("${ccd.results.usr.uri:http://localhost:8080/ccd-ws/algorithm/results/usr}") String userResultsUri,
            @Value("${ccd.results.file.usr.uri:http://localhost:8080/ccd-ws/algorithm/results/file/usr}") String userResultFileDownloadUri) {
        this.webapp = webapp;
        this.appId = appId;
        this.userResultsUri = userResultsUri;
        this.userResultFileDownloadUri = userResultFileDownloadUri;
    }

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

    public List<ResultFileInfo> getUserResultFiles(String username) {
        List<ResultFileInfo> list = new LinkedList<>();

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

                ResultFileInfo info = new ResultFileInfo();
                info.setFileName(filename);
                info.setSize(size.toString());
                info.setCreationDate(FilePrint.fileTimestamp(creationTime));
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
