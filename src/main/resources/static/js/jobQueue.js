$(document).ready(function () {
    $("[data-toggle=tooltip]").tooltip();
});

$('#confirm-remove').on('show.bs.modal', function (e) {
    $(this).find('.btn-ok').attr('href', $(e.relatedTarget).data('href'));
});
