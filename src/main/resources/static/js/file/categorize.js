/*<![CDATA[*/
var fileTypes = [
    $('#dataTypeOpts'),
    $('#dataTypeOpts'),
    $('#varTypeOpts'),
    $('#knwlTypeOpts'),
    $('#resultTypeOpts')
];
$('input:radio[name="fileTypeId"]').click(function () {
    var opt = fileTypes[$(this).val()];
    $(".fileTypeOpt").not(opt).hide();
    opt.show();
});
$('input:radio[name="dataFileFormatId"]').click(function () {
    var val = $(this).val();
    switch (val) {
        case '1':
            $('#dataOpts').show();
            break;
        default :
            $('#dataOpts').hide();
    }
});
$(document).ready(function () {
    $('input:radio[name="fileTypeId"]:checked').click();
    $('input:radio[name="dataFileFormatId"]:checked').click();

    $('#title').editable({
        success: function (response, newValue) {
            if (response.status === 'error') {
                return response.msg;
            } else {
                $('#page_title').text(newValue);
            }
        }
    });
});
$(document).on('click', '#categorize_file', function (e) {
    collapseAction(this);
});
/*]]>*/
