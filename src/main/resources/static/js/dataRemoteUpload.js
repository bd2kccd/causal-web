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

function displaySize(size) {
    var units = ['TB', 'GB', 'MB', 'KB', 'b'];
    var filesizeBase = 1000;
    var selectedSize, selectedUnit;
    for (var i = 0; i < units.length; i++) {
        var unit = units[i];
        var cutoff = Math.pow(filesizeBase, 4 - i) / 10;
        if (size >= cutoff) {
            selectedSize = size / Math.pow(filesizeBase, 4 - i);
            selectedUnit = unit;
            break;
        }
    }
    selectedSize = Math.round(10 * selectedSize) / 10;

    return '<strong>' + selectedSize + selectedUnit + '</strong> ';
}

var ws_endpoint = $('#ws_endpoint').val();
var rRemote = new Resumable({
    target: ws_endpoint, //'http://localhost:9000/ccd-ws/chw20/data/upload/chunk?appId=',
    chunkSize: 1 * 128 * 1024,
    simultaneousUploads: 32
});

$(document).ready(function () {
    var dropZone = $('.remote-resumable-drop');
    var dropZoneBrowse = $('.remote-resumable-browse');

    // Resumable.js isn't supported, fall back on a different method
    if (!rRemote.support) {
        $('.remote-resumable-error').show();
    } else {
        rRemote.assignBrowse(dropZoneBrowse);
        rRemote.assignDrop(dropZone);
        rRemote.on('filesAdded', function (files) {
            var output = [];
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                var modifiedDate = file.lastModifiedDate ? file.lastModifiedDate.toLocaleDateString() : 'n/a';
                var info = '<tr><td>' + file.fileName + '</td><td>'
                        + displaySize(file.size) + '</td><td>'
                        + modifiedDate + '</td><td class="md5-'
                        + file.uniqueIdentifier + '"></td></tr>';
                output.push(info);
            }
            // Show progress bar
            $('.remote-resumable-progress, .remote-resumable-list').show();
            // Show pause and cancel, hide resume
            $('.remote-resumable-progress .remote-progress-resume-link').hide();
            $('.remote-resumable-progress .remote-progress-pause-link').show();
            $('.remote-resumable-progress .remote-progress-cancel-link').show();
            $('.remote-file-info > tbody:last').append(output.join(''));
            rRemote.upload();
        });
        rRemote.on('cancel', function () {
            $('.remote-resumable-progress .remote-progress-pause-link').hide();
            $('.remote-resumable-progress .remote-progress-resume-link').hide();
        });
        rRemote.on('pause', function () {
            $('.remote-resumable-progress .remote-progress-pause-link').hide();
            $('.remote-resumable-progress .remote-progress-resume-link').show();
        });
        rRemote.on('progress', function () {
            // Show resume, hide pause
            $('.remote-resumable-progress .remote-progress-resume-link').hide();
            $('.remote-resumable-progress .remote-progress-pause-link').show();
        });
        rRemote.on('complete', function () {
            // Hide pause/resume and cancel when the upload has completed
            $('.remote-progress-bar').html('(completed)');
            $('.remote-resumable-progress .remote-progress-resume-link, .remote-resumable-progress .remote-progress-pause-link, .remote-resumable-progress .remote-progress-cancel-link').hide();
        });
        rRemote.on('fileSuccess', function (file, message) {
            $('.md5-' + file.uniqueIdentifier).html(message);
        });
        rRemote.on('fileError', function (file, message) {
            // Reflect that the file upload has resulted in error
            $('.resumable-file-' + file.uniqueIdentifier + ' .remote-resumable-file-progress').html('(file could not be uploaded: ' + message + ')');
        });
        rRemote.on('fileProgress', function (file) {
            // Handle progress for both the file and the overall upload
            $('.remote-progress-bar').html(Math.floor(rRemote.progress() * 100) + '%');
            $('.remote-progress-bar').css({width: Math.floor(rRemote.progress() * 100) + '%'});
        });
    }

    //Show existing on-process uploading file progress
    $('.btn-psc-upload').each(function () {
        console.log($(this).attr('data-filename'));
        var fileName = $(this).attr('data-filename');
        $.post("data/remoteUpload/queue?fileName=" + fileName, function (data) {
            if (data == true) {
                $(this).hide();
                $.get("data/remoteUpload?fileName=" + fileName, function (data) {
                    var totalChunkNum = data;
                    var progressTextMessage = $(this).parent();
                    checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum);
                });
            }
        });
    });

});

$('#dataExample').on('show.bs.modal', function (event) {
    var link = $(event.relatedTarget);
    var title = link.data('title');
    var type = link.data('type');

    var modal = $(this);
    modal.find('.modal-title').text(title + ' Dataset');
    modal.find('#dataExampleFrame').attr('src', 'example?type=' + type);
});

function checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum) {
    $.post("data/remoteUpload?fileName=" + fileName, function (data) {
        if (jQuery.type(data) == "number") {

            var pullingDelay = 500;
            var progressMessage;
            if (data == 0) {
                progressMessage = "Queued";
            } else {
                progressMessage = "" + (Math.floor((data / totalChunkNum) * 10000) / 100).toFixed(2) + "%";

            }
            console.log(fileName + " : " + progressMessage);
            $(progressTextMessage).html(progressMessage);

            if (data < totalChunkNum) {
                window.setTimeout(
                        checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum)
                        , pullingDelay);
            } else if (data == totalChunkNum) {

            }

        } else {

        }
    });

}

function uploadDataToRemoteServer(btn, fileName) {
    $(btn).hide();
    $.get("data/remoteUpload?fileName=" + fileName, function (data) {
        var totalChunkNum = data;
        var progressTextMessage = $(btn).parent();
        checkFileUploadProgress(progressTextMessage, fileName, totalChunkNum);
    });
}

