$(document).ready(function () {
    $('#file_table').DataTable({
        "order": [[2, "desc"]],
        "lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
        "columnDefs": [
            {"orderable": false, "bSearchable": false, "targets": 1}
        ]
    });
});
