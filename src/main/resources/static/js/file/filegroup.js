var filegroup_tbl = $('#filegroup_tbl').DataTable({
    pageLength: 10,
    responsive: true,
    "aoColumns": [
        {"mData": "name"},
        {"mData": "creationTime"}
    ],
    "columnDefs": [
        {"width": "0", "className": "text-center", "targets": 1},
        {"orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 2},
        {"orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 3}
    ],
    "order": [[1, "desc"]]
});

$('#filegroup_tbl tbody').on('click', 'tr td .delete', function () {
    var rowNum = $(this).parents('tr')[0];
    var row = filegroup_tbl.row(rowNum);
    var node = row.node();
    var rowData = row.data();
    $('#delete_file_name').text(rowData['name']);
    $('#del_btn').data('id', row.index());
    $('#del_btn').data('href', $(node).find('.delete').data('href'));
    $('#confirm_delete').modal('toggle');
});

$(document).on('click', '#del_btn', function (e) {
    var uri = $('#del_btn').data('href');
    $.ajax({
        url: encodeURI(uri),
        type: 'DELETE',
        success: function () {
            var rowNum = $('#del_btn').data('id');
            var row = filegroup_tbl.row(rowNum);

            row.remove().draw();
        }
    });
});

$('body').tooltip({selector: '[data-toggle="tooltip"]'});
