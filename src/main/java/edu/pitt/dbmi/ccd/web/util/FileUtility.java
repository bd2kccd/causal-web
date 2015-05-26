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
package edu.pitt.dbmi.ccd.web.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import edu.pitt.dbmi.ccd.web.model.FileInfo;
import edu.pitt.dbmi.ccd.web.model.FileMetadata;
import edu.pitt.dbmi.ccd.web.util.FileUtility;

/**
 *
 * May 18, 2015 1:05:35 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileUtility {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private FileUtility() {
    }

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String humanReadableSize(long fileSize, boolean si) {
        int unit = si ? 1000 : 1024;
        if (fileSize < unit) {
            return fileSize + " B";
        }
        int exp = (int) (Math.log(fileSize) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

        return String.format("%.2f %sB", fileSize / Math.pow(unit, exp), pre);
    }

    public static List<FileMetadata> getFileListing(String directory){
    	List<FileMetadata> list = new LinkedList<>();
    	
    	List<FileInfo> fileList = new LinkedList<>();
    	try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
    		for (Path path : directoryStream) {
    			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    			FileInfo info = new FileInfo();
    			info.setFileName(path.getFileName().toString());
    			info.setFilePath(path.toAbsolutePath().toString());
    			info.setCreationTime(new Date(attrs.creationTime().toMillis()));
    			info.setLastAccessTime(new Date(attrs.lastAccessTime().toMillis()));
    			info.setLastModifiedTime(new Date(attrs.lastModifiedTime().toMillis()));
                info.setFileSize(attrs.size());
                //info.setMd5CheckSum(MessageDigestHash.computeMD5Hash(path));
                fileList.add(info);
    		}
        } catch (IOException exception) {
        }
    	
    	Collections.sort(fileList, new FileDateSort());
    	for(FileInfo info : fileList){
    		FileMetadata resultFile = new FileMetadata();
    		resultFile.setFileName(info.getFileName());
    		resultFile.setLastModifiedDate(FileUtility.formatDate(info.getLastModifiedTime()));
    		resultFile.setSize(FileUtility.humanReadableSize(info.getFileSize(), true));
    		list.add(resultFile);
    	}
    	
    	return null;
    }
    
    private static class FileDateSort implements Comparator<FileInfo>{

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(FileInfo o1, FileInfo o2) {
			return o1.getLastModifiedTime().compareTo(o2.getLastModifiedTime());
		}
    	
    }
}
