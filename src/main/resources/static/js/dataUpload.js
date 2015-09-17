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

var r = new Resumable({
    target: 'upload/chunk',//'http://manojlaptop:8080/ccd-ws/kvb2/data/upload/chunk', //'http://localhost:9000/ccd-ws/chw20/data/upload/chunk?appId=',//
    chunkSize: 1 * 1024 * 1024,
    simultaneousUploads: 4
});

$(document).ready(function () {
    var dropZone = $('.resumable-drop');
    var dropZoneBrowse = $('.resumable-browse');

    // Resumable.js isn't supported, fall back on a different method
    if (!r.support) {
        $('.resumable-error').show();
    } else {
        r.assignBrowse(dropZoneBrowse);
        r.assignDrop(dropZone);
        r.on('filesAdded', function (files) {
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
            $('.resumable-progress, .resumable-list').show();
            // Show pause and cancel, hide resume
            $('.resumable-progress .progress-resume-link').hide();
            $('.resumable-progress .progress-pause-link').show();
            $('.resumable-progress .progress-cancel-link').show();
            $('.file-info > tbody:last').append(output.join(''));
            r.upload();
        });
        r.on('cancel', function () {
            $('.resumable-progress .progress-pause-link').hide();
            $('.resumable-progress .progress-resume-link').hide();
        });
        r.on('pause', function () {
            $('.resumable-progress .progress-pause-link').hide();
            $('.resumable-progress .progress-resume-link').show();
        });
        r.on('progress', function () {
            // Show resume, hide pause
            $('.resumable-progress .progress-resume-link').hide();
            $('.resumable-progress .progress-pause-link').show();
        });
        r.on('complete', function () {
            // Hide pause/resume and cancel when the upload has completed
            $('.progress-bar').html('(completed)');
            $('.resumable-progress .progress-resume-link, .resumable-progress .progress-pause-link, .resumable-progress .progress-cancel-link').hide();
        });
        r.on('fileSuccess', function (file, message) {
            $('.md5-' + file.uniqueIdentifier).html(message);
        });
        r.on('fileError', function (file, message) {
            // Reflect that the file upload has resulted in error
            $('.resumable-file-' + file.uniqueIdentifier + ' .resumable-file-progress').html('(file could not be uploaded: ' + message + ')');
        });
        r.on('fileProgress', function (file) {
            // Handle progress for both the file and the overall upload
            $('.progress-bar').html(Math.floor(r.progress() * 100) + '%');
            $('.progress-bar').css({width: Math.floor(r.progress() * 100) + '%'});
        });
    }
    
});

$('#dataExample').on('show.bs.modal', function (event) {
    var link = $(event.relatedTarget);
    var title = link.data('title');
    var type = link.data('type');

    var modal = $(this);
    modal.find('.modal-title').text(title + ' Dataset');
    modal.find('#dataExampleFrame').attr('src', 'example?type=' + type);
});
