$(document).ready(function () {
    var job_queue_tbl = $('#job_queue_tbl').DataTable({
        "sAjaxSource": ws_path,
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": function (data) {
                    return '<a href="' + job_info_path + data.id + '">' + data.name + '</a>';
                }
            },
            {"mData": "creationTime"},
            {"mData": "status"},
            {"mData": "location"},
            {"mData": "status"}
        ],
        "columnDefs": [
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "targets": 1
            },
            {"render": function (data) {
                    if ('Canceled' == data || 'Finished' == data || 'Terminated' == data) {
                        return '<button class="btn btn-danger btn-xs cancel" data-placement="top" data-toggle="tooltip" title="Cancel Job" disabled="disabled">'
                                + '<span class="glyphicon glyphicon-trash"></span>'
                                + '</button>';
                    } else {
                        return '<button class="btn btn-danger btn-xs cancel" data-placement="top" data-toggle="tooltip" title="Cancel Job">'
                                + '<span class="glyphicon glyphicon-trash"></span>'
                                + '</button>';
                    }
                }, "orderable": false, "bSearchable": false, "targets": 4
            }
        ],
        "order": [[1, "desc"]]
    });

    $('#job_queue_tbl tbody').on('click', 'tr td .cancel', function () {
        var rowNum = $(this).parents('tr')[0];
        var row = job_queue_tbl.row(rowNum);
        var rowData = row.data();
        $('#confirm_cancel').find('.modal-title').text('Job Cancel Request: ' + rowData['name']);
        $('#cancel_btn').data('id', row.index());
        $('#confirm_cancel').modal('toggle');
    });

    $(document).on('click', '#cancel_btn', function (e) {
        var rowNum = $('#cancel_btn').data('id');
        var row = job_queue_tbl.row(rowNum);
        var rowData = row.data();

        $.ajax({
            url: ws_path + rowData['id'],
            type: 'DELETE',
            success: function (data) {
                row.data(data);
            }
        });
    });

    $('body').tooltip({selector: '[data-toggle="tooltip"]'});
});