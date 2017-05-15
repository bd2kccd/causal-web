/*<![CDATA[*/
var fmtOpts = [
    $('#dataOpts'),
    $('#dataOpts'),
    $('#varOpts'),
    $('#knwlOpts'),
    $('#resultOpts')
];
function fileTypeAction(chkOpt) {
    for (i = 0; i < fmtOpts.length; i++) {
        fmtOpts[i].hide();
    }
    var opts = fmtOpts[$(chkOpt).val()];
    opts.show();

    var optRadios = opts.find('input:radio');
    if (!optRadios.prop('checked')) {
        optRadios[0].checked = true;
    }
}
$(document).ready(function () {
    $('#title').editable({
        success: function (response, newValue) {
            if (response.status === 'error') {
                return response.msg;
            } else {
                $('#page_title').text(newValue);
            }
        }
    });
    fileTypeAction($("input[name='fileTypeId']:checked"));
});
/*]]>*/
