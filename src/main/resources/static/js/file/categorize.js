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
    fileFormatAction($("input[name='fileFormatId']:checked"));
}
function fileFormatAction(chkOpt) {
    if ($(chkOpt).val() == 1) {
        $('#tetradDataOpts').show();
    } else {
        $('#tetradDataOpts').hide();
    }

    var optRadios = $('#tetradDataOpts').find('input[name=fileDelimiterTypeId]:radio');
    if (!optRadios.prop('checked')) {
        optRadios[0].checked = true;
    }
    optRadios = $('#tetradDataOpts').find('input[name=fileVariableTypeId]:radio');
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
    fileFormatAction($("input[name='fileFormatId']:checked"));
});
/*]]>*/
