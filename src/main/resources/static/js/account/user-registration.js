$(document).ready(function () {
    $('#register').prop('disabled', !$('#agree').prop('checked'));

    $('#agree').click(function () {
        $('#register').prop('disabled', !$(this).prop('checked'));
    })
});