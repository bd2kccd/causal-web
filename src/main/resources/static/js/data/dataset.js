$(document).ready(function () {
    $("[data-toggle=tooltip]").tooltip();
});

$('#confirm-delete').on('show.bs.modal', function (e) {
    $(this).find('.btn_ok').attr('href', $(e.relatedTarget).data('href'));
});

$('#fileInfo').on('show.bs.modal', function (event) {
    var link = $(event.relatedTarget);
    var filename = link.data('filename');

    var modal = $(this);
    modal.find('.modal-title').text(filename);
    modal.find('#fileInfoFrame').attr('src', 'data/fileInfo?file=' + filename);
});
