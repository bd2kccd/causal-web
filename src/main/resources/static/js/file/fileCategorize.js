$('input:radio[name="fileFormatId"]').click(function () {
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
    $('input:radio[name="fileFormatId"]:checked').click();

    $('#title').editable({
        validate: function (val) {
            if (val.trim().length === 0) {
                return 'Please enter a title.';
            }
        },
        success: function (response, newValue) {
            if (response.status === 'error') {
                return response.msg;
            } else {
                $('#page_title').text(newValue);
            }
        }
    });
});
