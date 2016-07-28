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
                var info = '<tr><td>' + file.fileName + '</td><td>'
                        + displaySize(file.size) + '</td><td class="status-'
                        + file.uniqueIdentifier +'"></td><td class="md5-'
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
            $('.status-' + file.uniqueIdentifier).html('Uploading');
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
            $('.status-' + file.uniqueIdentifier).html('Completed');
            $('.md5-' + file.uniqueIdentifier).html(message);
        });
        r.on('fileError', function (file, message) {
            $('.status-' + file.uniqueIdentifier).html(message);
        });
        r.on('fileProgress', function (file) {
            // Handle progress for both the file and the overall upload
            $('.progress-bar').html(Math.floor(r.progress() * 100) + '%');
            $('.progress-bar').css({width: Math.floor(r.progress() * 100) + '%'});
        });
    }
});
