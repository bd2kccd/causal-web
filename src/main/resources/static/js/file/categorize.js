function fileTypeAction(chkOpt) {
    var id = $(chkOpt).attr('value');
    if (id == dataTypeId) {
        $('#datasetOptions').removeClass('none');
    } else {
        $('#datasetOptions').addClass('none');
    }
}
function switchFileInfoForm() {
    $("#view_file_info").toggle();
    $("#edit_file_info").toggle();
}
$(document).ready(function () {
    fileTypeAction($("input[name='fileTypeId']:checked"));
});
$(document).on('click', '#categorize_file', function (e) {
    collapseAction(this);
});
