$(document).ready(function () {
    var fileTable = $('#file_table').DataTable({
        "sAjaxSource": listUrl,
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": function (data) {
                    return '<a href="' + categoryUrl + data.id + '">' + data.title + '</a>';
                }},
            {"mData": "fileSize"},
            {"mData": "creationTime"}
        ],
        "columnDefs": [
            {"render": function (data) {
                    return toHumanReadableSize(data);
                }, "type": 'file-size', "targets": 1
            },
            {"render": function (data) {
                    return moment(data).format('MM/DD/YYYY hh:mm:ss a');
                }, "targets": 2
            },
            {"render": function () {
                    return '<button class="btn btn-danger btn-xs delete">'
                            + '<span class="glyphicon glyphicon-trash"></span>'
                            + '</button>';
                }, "orderable": false, "bSearchable": false, "targets": 3
            }
        ],
        "order": [[0, "asc"]]
    });

    $('#file_table tbody').on('click', 'tr td .delete', function () {
        var rowNum = $(this).parents('tr')[0];
        var row = fileTable.row(rowNum);
        var rowData = row.data();
        $('#confirm_delete').find('.modal-title').text('Delete File: ' + rowData['title']);
        $('#del_btn').data('id', row.index());
        $('#confirm_delete').modal('toggle');
    });

    $(document).on('click', '#del_btn', function (e) {
        var rowNum = $('#del_btn').data('id');
        var row = fileTable.row(rowNum);
        var rowData = row.data();

        $.ajax({
            url: deleteUrl,
            data: {"id": rowData['id']},
            type: 'POST',
            success: function () {
                row.remove().draw();
            }
        });
    });
});
