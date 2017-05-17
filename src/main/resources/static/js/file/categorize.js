/*<![CDATA[*/
$('input:radio[name="fileTypeId"]').click(function () {
    var val = $(this).val();

    var opt;
    switch (val) {
        case '1':
            opt = $('#dataOpts');
            break;
        case '2':
            opt = $('#varOpts');
            break;
        case '3':
            opt = $('#knwlOpts');
            break;
        case '4':
            opt = $('#resultOpts');
            break;
        default :
            opt = $('#dataOpts');
    }
    $(".fileTypeOpt").not(opt).hide();
    $(opt).show();
    opt.find('input:radio[name="fileFormatId"]:first').click();
});
$('input:radio[name="fileFormatId"]').click(function () {
    var val = $(this).val();
    switch (val) {
        case '1':
            $('#delimOpts').show();
            $('#delimOpts').find('input:radio[name="fileDelimiterTypeId"]:first').click();
            $('#delimOpts').find('input:radio[name="fileVariableTypeId"]:first').click();
            break;
        default :
            $('#delimOpts').hide();
    }
});
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
    $('input[name="fileTypeId"]:first').click();
});
$(document).on('click', '#categorize_file', function (e) {
    collapseAction(this);
});
/*]]>*/
