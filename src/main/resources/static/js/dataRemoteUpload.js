function doNothing() {
}

function cancel(id, url) {
    var requestUrl = url + '/cancel';
    $.post(requestUrl, {id: id}, function () {
        var location = window.location.href;
        window.location.href = location;
    });
}

function getDownloadProgress(url) {
    var listUrl = url + '/list';
    $.get(listUrl, function (list) {
        for (var i = 0, len = list.length; i < len; i++) {
            var data = list[i];
            var id = data.id;

            if (!data.paused) {
                var btn = document.getElementById('btn_' + id);
                $(btn).toggleClass('btn-warning');
                $(btn).children('span').toggleClass('glyphicon-pause');
            }

            var progress = document.getElementById('prg_' + id);
            if ($(progress).is(':hidden')) {
                $(progress).show();
            }

            checkUploadStatus(id, url, document.getElementById('prgbar_' + id));
        }
    }).fail(function () {
        alert('Unable to get file upload status.')
    });
}

function pause(id, url) {
    var requestUrl = url + '/pause';
    $.post(requestUrl, {id: id});
}

function checkUploadStatus(id, url, progress) {
    var pullingDelay = 500;
    var requestUrl = url + '/status?' + $.param({id: id});
    $.get(requestUrl, function (data) {
        if (data === 100) {
            var location = window.location.href;
            window.location.href = location;
        } else {
            $(progress).css('width', data + '%').attr('aria-valuenow', data);
            $(progress).text(data + '% Complete (success)');
            window.setTimeout(checkUploadStatus(id, url, progress), pullingDelay);
        }
    });
}

function startUpload(id, fileName, url) {
    var btn = document.getElementById('btn_' + id);
    $(btn).toggleClass('btn-warning');
    $(btn).children('span').toggleClass('glyphicon-pause');

    var progress = document.getElementById('prg_' + id);
    if ($(progress).is(':hidden')) {
        $(progress).show();
    }

    if ($(btn).children('span').hasClass('glyphicon-pause')) {
        var requestUrl = url + '/upload';
        $.post(requestUrl, {id: id, fileName: fileName}, function () {
            checkUploadStatus(id, url, document.getElementById('prgbar_' + id));
        });
    } else {
        pause(id, url);
    }
}
