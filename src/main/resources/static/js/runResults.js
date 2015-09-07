$(document).ready(function () {
    $('[data-toggle=tooltip]').tooltip();

    $('#selecctall').change(function () {
        $('.checkResult').not('[disabled]').prop('checked', $(this).prop('checked'));
    });
});

$('#delete_btn').click(function () {
    if ($('input.checkResult:checkbox:checked').length == 0) {
        $('#errorSelection').modal('show');
    } else {
        $('#confirm-delete').modal('show');
    }
});

$('#compare_btn').click(function () {
    if ($('input.checkResult:checkbox:checked').length == 0) {
        $('#errorSelection').modal('show');
    } else {
        var url = $('#resultAction').attr('action') + '?action=compare';
        $('#resultAction').attr('action', url);
        $('form').submit();
    }
});

$('#confirm-delete').on('show.bs.modal', function (e) {
    $('.btn-ok').attr('href', 'delete');
});

$('#errorModal').on('show.bs.modal', function (e) {
    $(this).find('#errorModalFrame').attr('src', $(e.relatedTarget).data('href'));
});

$('.btn-ok').click(function () {
    var url = $('#resultAction').attr('action') + '/' + $('.btn-ok').attr('href');
    $('#resultAction').attr('action', url);
    $('.btn-ok').removeAttr('href');
    $('form').submit();
});
