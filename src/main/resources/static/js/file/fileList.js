$(document).ready(function () {
    $("[data-toggle=tooltip]").tooltip();

    $('#file_table').DataTable({
        "order": [[2, "desc"]],
        responsive: true,
        "lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
        "columnDefs": [
            {"orderable": false, "bSearchable": false, "targets": 1},
            {"orderable": false, "bSearchable": false, "targets": 3}
        ]
    });
});
