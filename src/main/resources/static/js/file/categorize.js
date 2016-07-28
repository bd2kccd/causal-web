function fileTypeAction(chkOpt) {
    var id = $(chkOpt).attr('value');
    if (id === '1') {
        $('#datasetOptions').removeClass('none');
    } else {
        $('#datasetOptions').addClass('none');
    }
}
$(document).ready(function () {
    fileTypeAction($("input[name='fileTypeId']:checked"));
});
$(document).on('click', '#collapseLink', function (e) {
    var $this = $(this);
    if ($this.hasClass('collapsed')) {
        $this.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
    } else {
        $this.find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
    }
});
