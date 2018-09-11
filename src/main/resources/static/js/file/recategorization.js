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
    $("#edit").click(function () {
        $("#view_panel").hide();
        $("#edit_panel").show();
    });
    $("#cancel_edit").click(function () {
        $("#view_panel").show();
        $("#edit_panel").hide();
    });
    $('input:radio[name="formatOpt"]:checked').click();
});
