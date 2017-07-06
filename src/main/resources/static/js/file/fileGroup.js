$('select[id="continuousFileIds"]').bootstrapDualListbox();
$('select[id="discreteFileIds"]').bootstrapDualListbox();
$('select[id="mixedFileIds"]').bootstrapDualListbox();

$('input:radio[name="fileVariableTypeId"]').click(function () {
    var val = $(this).val();
    var opt;
    switch (val) {
        case '1':
            opt = $('#continuousData');
            break;
        case '2':
            opt = $('#discreteData');
            break;
        default :
            opt = $('#mixedData');
    }
    $(".dataOpts").not(opt).hide();
    $(".dataOpts").find('option').prop('selected', false);

    $('select[id="continuousFileIds"]').bootstrapDualListbox('refresh', true);
    $('select[id="discreteFileIds"]').bootstrapDualListbox('refresh', true);
    $('select[id="mixedFileIds"]').bootstrapDualListbox('refresh', true);

    opt.show();
});

$(document).ready(function () {
    $('input:radio[name="fileVariableTypeId"]:checked').click();
});
