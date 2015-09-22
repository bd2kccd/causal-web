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

$(document).ready(function () {	
    
    $('.btn-psc-upload').click(function(){
    	var fileName = $(this).attr('data-filename');
    	uploadDataToRemoteServer($(this), fileName);
    });
    
    //Show existing on-process uploading file progress
    $('.btn-psc-upload').each(function(){
    	console.log($(this).attr('data-filename'));
    	var fileName = $(this).attr('data-filename');
    	$.post("data/remoteUpload/queue?fileName=" + fileName, function(data){
    		if(data == true){
    			//uploadDataToRemoteServer($(this), fileName);
    			//$(this).click();
    			//uploadDataToRemoteServer($('#btn' + fileName), fileName);
    			//$('#btn' + fileName).click();
    		}
    	});
    });
    
});

function checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum){
	$.post("data/remoteUpload?fileName=" + fileName, function(data){
		if(jQuery.type(data) == "number"){
			
			var pullingDelay = 500;
			var progressMessage;
			if(data == 0){
				progressMessage = "Queued";
			}else{
				progressMessage = "" + (Math.floor((data / totalChunkNum) * 10000)/100).toFixed(2) + "%";
				
			}
			console.log(fileName + " : " + progressMessage);
			$(progressTextMessage).html(progressMessage);
			
			if(data < totalChunkNum){
				window.setTimeout(
						checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum)	
						, pullingDelay);
			}else if(data == totalChunkNum){
				
			}
			
		}else{
			
		}
	});
	
}

function uploadDataToRemoteServer(btn, fileName){
	$(btn).hide();
	$.get("data/remoteUpload?fileName=" + fileName, function(data){
		var totalChunkNum = data;
		var progressTextMessage = $(btn).parent();
		checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum);
	});
}
