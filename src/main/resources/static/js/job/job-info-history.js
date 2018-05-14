$(document).ready(function () {
    var job_history_tbl = $('#job_history_tbl').DataTable({
        "sAjaxSource": ws_path + 'history',
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": function (data) {
                    return '<a href="' + job_info_path + data.id + '">' + data.name + '</a>';
                }
            },
            {"mData": "algoType"},
            {"mData": "creationTime"},
            {"mData": "status"}
        ],
        "columnDefs": [
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "targets": [2]
            }
        ],
        "rowCallback": function (row, data, index) {
            switch (data.status) {
                case 'Failed':
                    $(row).addClass('danger');
                    break;
                case 'Cancel':
                    $(row).addClass('warning');
                    break;
                case 'Finished':
                    $(row).addClass('success');
                    break;
            }
        },
        "order": [[2, "desc"]]
    });
});