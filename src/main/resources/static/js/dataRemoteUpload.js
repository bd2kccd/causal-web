function checkUploadStatus(requestUrl, progressMsg) {
    var pullingDelay = 500;
    $.post(requestUrl, function (data) {
        progressMsg.text(data + '%');
        if (data !== 100) {
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