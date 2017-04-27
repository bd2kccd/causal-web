$(document).ready(function () {
    $("#view_info_panel").show();
    $("#edit_info_panel").hide();

    $("#edit_info").click(function () {
        $("#view_info_panel").hide();
        $("#edit_info_panel").show();
    });
    $("#cancel_info").click(function () {
        $("#view_info_panel").show();
        $("#edit_info_panel").hide();
    });
});
$(document).on('click', '#collapse_link', function (e) {
    collapseAction(this);
});
