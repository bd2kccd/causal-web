$('input:radio[name="formatOpt"]').click(function () {
    var val = parseInt($(this).val());
    switch (val) {
        case dataGroupId:
            $('#tetrad_tab_data').show();
            break;
        default :
            $('#tetrad_tab_data').hide();
    }
});
$(document).ready(function () {
    $('input:radio[name="formatOpt"]:checked').click();
});
