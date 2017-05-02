$(document).ready(function () {
    $('#file_table').DataTable({
        "order": [[2, "desc"]],
        "lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
        "columnDefs": [
            {type: 'file-size', targets: 1},
            {"orderable": false, "bSearchable": false, "targets": 3}
        ]
    });
});
$('#confirm-delete').on('show.bs.modal', function (e) {
    $(this).find('.modal-title').text('Delete File: ' + $(e.relatedTarget).data('title'));
    $('input[name="id"]').val($(e.relatedTarget).data('id'));
});
