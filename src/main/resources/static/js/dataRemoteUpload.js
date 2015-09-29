function getDownloadProgress(url) {
    var listUrl = url + '/list';
    $.get(listUrl, function (list) {
        for (var i = 0, len = list.length; i < len; i++) {
            var requestUrl = url + '?fileName=' + list[i];
            var btn = document.getElementById(list[i]);
            var progressMsg = $(btn).parent();
            checkUploadStatus(requestUrl, progressMsg);
        }
    }).fail(function () {
        alert('Unable to get file upload status.')
    });
}

function checkUploadStatus(requestUrl, progressMsg) {
    var pullingDelay = 500;
    $.post(requestUrl, function (data) {
        if (data === 100) {
            var location = window.location.href;
            window.location.href = location;
        } else {
            progressMsg.text(data + '%');
            window.setTimeout(checkUploadStatus(requestUrl, progressMsg), pullingDelay);
        }
    });
}

function startUpload(btn, fileName, url) {
    var requestUrl = url + '?fileName=' + fileName;
    $(btn).hide();
    var progressMsg = $(btn).parent();
    $.get(requestUrl, function () {
        progressMsg.text('Queued');
        checkUploadStatus(requestUrl, progressMsg);
    }).fail(function () {
        alert('File \'' + fileName + '\' does not exist!');
        var location = window.location.href;
        window.location.href = location;
    });
}