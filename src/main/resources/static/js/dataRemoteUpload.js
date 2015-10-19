function doNothing() {
}

function getDownloadProgress(url) {
    var listUrl = url + '/list';
    $.get(listUrl, function (list) {
        for (var i = 0, len = list.length; i < len; i++) {
            var requestUrl = url + '?id=' + list[i];
            var btn = document.getElementById('btn_' + list[i]);
            var progressMsg = $(btn).parent();
            checkUploadStatus(requestUrl, progressMsg);
        }
    }).fail(function () {
        alert('Unable to get file upload status.')
    });
}

function checkUploadStatus(url, id, progressMsg) {
    var pullingDelay = 500;
    var params = {id: id};
    var requestUrl = url + '/status?' + $.param(params);
    $.get(requestUrl, function (data) {
        if (data === 100) {
            var location = window.location.href;
            window.location.href = location;
        } else {
            progressMsg.text(data + '%');
            window.setTimeout(checkUploadStatus(url, id, progressMsg), pullingDelay);
        }
    });
}

function startUpload(btn, id, fileName, url) {
    var requestObj = new Object();
    requestObj.id = id;
    requestObj.fileName = fileName;

    var requestUrl = url + '/upload';
    var progressMsg = $(btn).parent();

    $.post(requestUrl, {id: id, fileName: fileName}, function () {
        $(btn).hide();
        progressMsg.text('Queued');
        checkUploadStatus(url, id, progressMsg);
    }).fail(function () {
        alert('Unable to upload \'' + fileName + '\'!');
        var location = window.location.href;
        window.location.href = location;
    });
}