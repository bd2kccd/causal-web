$(document).ready(function () {
    $("[data-toggle=tooltip]").tooltip();

    $('#file_table').DataTable({
        "order": [[2, "desc"]],
        responsive: true,
        "lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
        "columnDefs": [
            {"orderable": false, "bSearchable": false, "targets": 3}
        ]
    });
});
$('#confirm-delete').on('show.bs.modal', function (e) {
    $(this).find('.modal-title').text('Delete File: ' + $(e.relatedTarget).data('title'));
    $(this).find('#btn_ok').attr('href', $(e.relatedTarget).data('href'));
});
