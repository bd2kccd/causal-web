$(document).ready(function () {
    var fileTable = $('#filegroup_table').DataTable({
        "sAjaxSource": listUrl,
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": function (data) {
                    return '<a href="' + editGroupFileUrl + data.id + '">' + data.name + '</a>';
                }
            }
        ],
        "columnDefs": [
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "targets": 1
            },
            {"render": function () {
                    return '<button class="btn btn-danger btn-xs delete" data-placement="top" data-toggle="tooltip" title="Delete File">'
                            + '<span class="glyphicon glyphicon-trash"></span>'
                            + '</button>';
                }, "orderable": false, "bSearchable": false, "targets": 2
            }
        ],
        "order": [[0, "asc"]]
    });

    $('#filegroup_table tbody').on('click', 'tr td .delete', function () {
        var rowNum = $(this).parents('tr')[0];
        var row = fileTable.row(rowNum);
        var rowData = row.data();
        $('#confirm_delete').find('.modal-title').text('Delete File: ' + rowData['name']);
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

    $('body').tooltip({selector: '[data-toggle="tooltip"]'});
});
