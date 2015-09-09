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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import edu.pitt.dbmi.ccd.web.service.cloud.dto.WebServiceHealth;

/**
 * 
 * Aug 27, 2015 3:37:33 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@Service
public class CloudService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudService.class);

	private final Boolean webapp;

	private final RestTemplate restTemplate;

	private final String webServiceHealthUri;

	private final String appId;

	/**
	 * @param webapp
	 * @param restTemplate
	 * @param webServiceHealthUri
	 * @param appId
	 */
	@Autowired(required = true)
	public CloudService(Boolean webapp, RestTemplate restTemplate,
			@Value("${ccd.ws.health:http://localhost:8080/ccd-ws/health}") String webServiceHealthUri,
			@Value("${ccd.rest.appId:1}") String appId) {
		this.webapp = webapp;
		this.restTemplate = restTemplate;
		this.webServiceHealthUri = webServiceHealthUri;
		this.appId = appId;
	}

	public boolean isWebServiceOnline() {

		if (!webapp) {
			try {
				ResponseEntity<WebServiceHealth> responseEntity = restTemplate.getForEntity(webServiceHealthUri, WebServiceHealth.class);
				if (responseEntity.getStatusCode() == HttpStatus.OK) {
					WebServiceHealth webServiceHealth = responseEntity.getBody();
					if(webServiceHealth.getStatus().compareTo("UP") != 0){
						return false;
					}
				}
			} catch (RestClientException exception) {
	            LOGGER.error(exception.getMessage());
	            return false;
	        }
		}
			
		return true;
		
	}
	
}
