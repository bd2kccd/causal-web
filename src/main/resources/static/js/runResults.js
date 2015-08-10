$(document).ready(function () {
    $("[data-toggle=tooltip]").tooltip();
});

$('#confirm-delete').on('show.bs.modal', function (e) {
    $(this).find('.btn-ok').attr('href', $(e.relatedTarget).data('href'));
});

$('#errorModal').on('show.bs.modal', function (e) {
    $(this).find('#errorModalFrame').attr('src', $(e.relatedTarget).data('href'));
});
