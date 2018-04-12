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
            {"mData": "location"}
        ],
        "columnDefs": [
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "targets": 1
            },
            {"render": function () {
                    return '<button class="btn btn-danger btn-xs" data-placement="top" data-toggle="tooltip" title="Cancel Job">'
                            + '<span class="glyphicon glyphicon-stop"></span>'
                            + '</button>';
                }, "orderable": false, "bSearchable": false, "targets": 4
            }
        ],
        "order": [[1, "desc"]]
    });
});