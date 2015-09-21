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
package edu.pitt.dbmi.ccd.web.ctrl.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import edu.pitt.dbmi.ccd.web.domain.AppUser;

/**
 * 
 * Sep 11, 2015 5:23:01 PM
 * 
 * @author Chirayu (Kong) Wongchokprasitti
 *
 */
@RestController
@SessionAttributes("appUser")
@RequestMapping(value = "data/remoteUpload")
public class DataRemoteUploadController {
 
	private static final Logger LOGGER = LoggerFactory.getLogger(DataRemoteUploadController.class);
	
	private final String userDataRemoteUploadUri;
	
    private final String appId;

    private final ExecutorService fileUploadExecutor;
    
    private final HashMap<String,HashSet<Integer>> fileUploadProgressMap;

	@Autowired(required = true)
	public DataRemoteUploadController(
			@Value("${ccd.data.upload.uri:http://localhost:9000/ccd-ws/%s/data/upload/chunk?appId=%s}") String userDataRemoteUploadUri,
			@Value("${ccd.rest.appId:1}") String appId, 
			@Value("${ccd.data.upload.simultaneousFileUploads:1}") final int simultaneousFileUploads){
		this.userDataRemoteUploadUri = userDataRemoteUploadUri;
		this.appId = appId;
		this.fileUploadExecutor = Executors.newFixedThreadPool(simultaneousFileUploads);
		this.fileUploadProgressMap = new HashMap<>();
	}

	@RequestMapping(value = "queue", method = RequestMethod.POST)
	public ResponseEntity<Boolean> checkExistingUploadingFileInQueue(
			@RequestParam("fileName") String fileName){
		Boolean fileInQueue = false;
		if(fileUploadProgressMap.get(fileName) != null){
			fileInQueue = true;
		}
		return new ResponseEntity<>(fileInQueue, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<Integer> initiateDataRemoteUpload(
			@RequestParam("fileName") String fileName,
			@Value("${ccd.data.upload.sizeThreshold:10240}") int sizeThreshold,
			@Value("${ccd.data.upload.simultaneousChunkUploads:32}") final int simultaneousChunkUploads,
			@ModelAttribute("appUser") AppUser appUser) throws URISyntaxException, Exception{
		
		Path dataFilePath = Paths.get(appUser.getDataDirectory(), fileName);
		
		BasicFileAttributes attrs = Files.readAttributes(dataFilePath, BasicFileAttributes.class);
		
		long resumableTotalSize = attrs.size();
		int resumableTotalChunks = ((int) (resumableTotalSize/sizeThreshold)) + 
				(((int)resumableTotalSize)%sizeThreshold == 0?0:1);

		//If an uploading file not already in the queue, put it there
		if(fileUploadProgressMap.get(fileName) == null){
			fileUploadProgressMap.put(fileName, new HashSet<Integer>());
			LOGGER.info("Uploading #chunk: " + resumableTotalChunks + " of " + fileName);
			
			//FileUpload Executor
			FileUploadRunnable fileUploadRunnable = new FileUploadRunnable(
					fileName, sizeThreshold, appUser.getDataDirectory(), simultaneousChunkUploads, 
					userDataRemoteUploadUri, appUser.getUsername(), appId, fileUploadProgressMap);
			fileUploadExecutor.execute(fileUploadRunnable);
		}
		
		return new ResponseEntity<>(resumableTotalChunks, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Integer> checkProgressFileUpload(
			@RequestParam("fileName") String fileName){
		
		Integer chunkNumTransfered = null;
		
		HashSet<Integer> chunkSet = fileUploadProgressMap.get(fileName);
		if(chunkSet != null){
			chunkNumTransfered = new Integer(chunkSet.size());
		}
		
		return new ResponseEntity<>(chunkNumTransfered, HttpStatus.OK);
	}	
    		
}

class FileUploadRunnable implements Runnable {
	
	private final String fileName;
	private final int sizeThreshold;
	private final String dataDirectory;
	private final int resumableTotalChunks;
	private final ExecutorService chunkUploadExecutor;
	private final String userDataRemoteUploadUri;
	private final String username;
	private final String appId;
	private final HashMap<String,HashSet<Integer>> fileUploadProgressMap;
	
	public FileUploadRunnable(String fileName, int sizeThreshold, String dataDirectory, int simultaneousChunkUploads, 
			String userDataRemoteUploadUri, String username, String appId, 
			HashMap<String,HashSet<Integer>> fileUploadProgressMap) throws IOException{
		this.fileName = fileName;
		this.sizeThreshold = sizeThreshold;
		this.dataDirectory = dataDirectory;
		//Calculate #chunk
		Path dataFilePath = Paths.get(dataDirectory, fileName);	
		BasicFileAttributes attrs = Files.readAttributes(dataFilePath, BasicFileAttributes.class);
		long resumableTotalSize = attrs.size();
		this.resumableTotalChunks = ((int) (resumableTotalSize/sizeThreshold)) + 
				(((int)resumableTotalSize)%sizeThreshold == 0?0:1);
		this.chunkUploadExecutor = Executors.newFixedThreadPool(simultaneousChunkUploads);
		this.userDataRemoteUploadUri = userDataRemoteUploadUri;
		this.username = username;
		this.appId = appId;
		this.fileUploadProgressMap = fileUploadProgressMap;
	}
	
	@Override
	public void run() {
		List<Future<Integer>> chunkList = new ArrayList<>();
		for(int resumableChunkNumber=1;resumableChunkNumber<=resumableTotalChunks;resumableChunkNumber++){
			System.out.println("Loading chunk#" + resumableChunkNumber);
			Callable<Integer> chunkUploadCallable = new ChunkUploadCallable(resumableChunkNumber, 
					fileName, sizeThreshold, dataDirectory, userDataRemoteUploadUri, username, appId);
			Future<Integer> submit = chunkUploadExecutor.submit(chunkUploadCallable);
			chunkList.add(submit);
		}
		
		for(Future<Integer> chunk : chunkList){
			try {
				
				if(chunk.get().intValue() > 0){//Notify back to user about which chunk is done
					HashSet<Integer> chunkSet = fileUploadProgressMap.get(fileName);
					chunkSet.add(new Integer(chunk.get().intValue()));
					fileUploadProgressMap.put(fileName, chunkSet);
				}else if(chunk.get().intValue() < 0){
					int resumableChunkNumber = -1*chunk.get().intValue();
					Callable<Integer> chunkUploadCallable = new ChunkUploadCallable(resumableChunkNumber, 
							fileName, sizeThreshold, dataDirectory, userDataRemoteUploadUri, username, appId);
					Future<Integer> submit = chunkUploadExecutor.submit(chunkUploadCallable);
					chunkList.add(submit);
				}else{//Terminate uploading process
					chunkUploadExecutor.shutdownNow();
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		chunkUploadExecutor.shutdown();
		
		try {
			chunkUploadExecutor.awaitTermination(resumableTotalChunks, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ChunkUploadCallable implements Callable<Integer> {
	
	private final int resumableChunkNumber;
	private final String fileName;
	private final int sizeThreshold;
	private final String dataDirectory;
	private final String userDataRemoteUploadUri;
	private final String username;
	private final String appId;

	public ChunkUploadCallable(int resumableChunkNumber, String fileName, int sizeThreshold, String dataDirectory, 
			String userDataRemoteUploadUri, String username, String appId){
		this.resumableChunkNumber = resumableChunkNumber;
		this.fileName = fileName;
		this.sizeThreshold = sizeThreshold;
		this.dataDirectory = dataDirectory;
		this.userDataRemoteUploadUri = userDataRemoteUploadUri;
		this.username = username;
		this.appId = appId;
	}
	
	@Override
	public Integer call() throws Exception{
    	Path dataFilePath = Paths.get(dataDirectory, fileName);
		BasicFileAttributes attrs = Files.readAttributes(dataFilePath, BasicFileAttributes.class);
		long resumableTotalSize = attrs.size();
		int resumableCurrentChunkSize = Math.min(sizeThreshold, (int) (resumableTotalSize - (resumableChunkNumber-1)*sizeThreshold));

		//Identifier = file size(byte) - file name with file extension but no dot
		String resumableIdentifier = String.valueOf(resumableTotalSize) + "-" + fileName.replace(".", "");

		int resumableTotalChunks = ((int) (resumableTotalSize/sizeThreshold)) + (((int)resumableTotalSize)%sizeThreshold == 0?0:1);
		
		String fileContentType = Files.probeContentType(dataFilePath);
		if(fileContentType == null){
			fileContentType = "text/plain";
		}
		
		Map<String, String> urlVariables = new HashMap<>();
		urlVariables.put("resumableIdentifier", resumableIdentifier);
		urlVariables.put("resumableCurrentChunkSize", String.valueOf(resumableCurrentChunkSize));
		urlVariables.put("resumableFilename", fileName);
		urlVariables.put("resumableType", fileContentType);
		urlVariables.put("resumableRelativePath", fileName);
		urlVariables.put("resumableChunkSize", String.valueOf(sizeThreshold));
		urlVariables.put("resumableChunkNumber", String.valueOf(resumableChunkNumber));
		urlVariables.put("resumableTotalChunks", String.valueOf(resumableTotalChunks));
		urlVariables.put("resumableTotalSize", String.valueOf(resumableTotalSize));
		
		List<String> params = new ArrayList<String>();
		urlVariables.forEach((key,value) -> {
			params.add(key + "=" + value);
		});
		
		//Send GET to remote server
		String uri = String.format(userDataRemoteUploadUri, username, appId);
		uri = uri + "&" + StringUtils.join(params, "&");
		//System.out.println("GET uri: " + uri);

		HttpGet httpGet = new HttpGet(uri);
		CloseableHttpClient httpClient = HttpClients.createMinimal();		
		CloseableHttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		
		int statusCode = response.getStatusLine().getStatusCode();
		
		EntityUtils.consume(entity);
		response.close();
		
		if(statusCode == 404){//Chunk Not Found, upload then
			
			byte[] allBytes = Files.readAllBytes(dataFilePath);
			byte[] chunk = new byte[resumableCurrentChunkSize];
			System.arraycopy(allBytes, (resumableChunkNumber-1)*sizeThreshold, chunk, 0, resumableCurrentChunkSize);

			uri = String.format(userDataRemoteUploadUri, username, appId);
	        HttpPost httpPost = new HttpPost(uri);
			//System.out.println("POST uri: " + uri);
	        
	        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	        builder.addTextBody("resumableIdentifier", resumableIdentifier);
	        builder.addTextBody("resumableCurrentChunkSize", String.valueOf(resumableCurrentChunkSize));
	        builder.addTextBody("resumableFilename", fileName);
	        builder.addTextBody("resumableType", fileContentType);
	        builder.addTextBody("resumableRelativePath", fileName);
	        builder.addTextBody("resumableChunkSize", String.valueOf(sizeThreshold));
	        builder.addTextBody("resumableChunkNumber", String.valueOf(resumableChunkNumber));
	        builder.addTextBody("resumableTotalChunks", String.valueOf(resumableTotalChunks));
	        builder.addTextBody("resumableTotalSize", String.valueOf(resumableTotalSize));
			
	        builder.addBinaryBody("file", chunk, ContentType.APPLICATION_OCTET_STREAM, "blob");
	        
	        HttpEntity multipart = builder.build();
	        
	        httpPost.setEntity(multipart);
	        
	        response = httpClient.execute(httpPost);
	        entity = response.getEntity();
			EntityUtils.consume(entity);
			response.close();
	        
		}else if(statusCode == 200){//Chunk is there, Do Nothing
			
			
		}else{
			httpClient.close();
			return -1*resumableChunkNumber;
		}
		
		httpClient.close();
		
		return resumableChunkNumber;
	}
}
