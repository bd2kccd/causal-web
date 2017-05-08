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
});
