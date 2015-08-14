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
package edu.pitt.dbmi.ccd.web.rest.ctrl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import edu.pitt.dbmi.ccd.db.entity.JobQueueInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.repository.JobQueueInfoRepository;
import edu.pitt.dbmi.ccd.db.repository.UserAccountRepository;
import edu.pitt.dbmi.ccd.web.CCDWebApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * 
 * Aug 13, 2015 3:57:11 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CCDWebApplication.class)
@WebAppConfiguration
public class JobQueueRestControllerTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;

	private String username = "chw20";

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private UserAccount userAccount;

	private JobQueueInfo jobQueueInfo;

	@Autowired
	UserAccountRepository userAccountRepository;

	@Autowired
	JobQueueInfoRepository jobQueueInforRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	public void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

		Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.jobQueueInforRepository.deleteAllInBatch();
		this.userAccount = this.userAccountRepository.findByUsername(username).get();
	}

	@Test
	public void userNotFound() throws Exception {
		mockMvc.perform(get("/ccd/chirayu/jobQueue/")).andExpect(status().isNotFound())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void submitJobQueue() throws Exception {
		this.jobQueueInfo = new JobQueueInfo(null, "GES", "", "very-big-data-with-one-zillion-variables", "tempDir",
				"outputDir", new Integer(0), new Date(System.currentTimeMillis()), Collections.singleton(userAccount));
		mockMvc.perform(post("/ccd/chw20/jobQueue/").contentType(contentType).content(json(jobQueueInfo)))
				.andExpect(status().isCreated());
	}

	@Test
	public void readSingleJobQueue() throws Exception {

	}

	@Test
	public void readJobQueue() throws Exception {
		mockMvc.perform(get("/ccd/chw20/jobQueue/")).andExpect(status().isOk())
				.andExpect(content().contentType(contentType));
	}

	@Test
	public void removeJobQueue() throws Exception {

	}

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
