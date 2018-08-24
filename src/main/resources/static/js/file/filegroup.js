var filegroup_tbl = $('#filegroup_tbl').DataTable({
    pageLength: 10,
    responsive: true,
    "sAjaxDataProp": "",
    "aoColumns": [
        {"mData": "name"},
        {"mData": "creationTime"},
        {"mData": function (source) {
                return {"id": source.id};
            }},
        {"mData": function (source) {
                return {"id": source.id};
            }}
    ],
    "columnDefs": [
        {"render": function (data) {
                return moment(data).format('MMM DD, YYYY hh:mm:ss A');
            }, "width": "10%", "targets": 1
        },
        {"render": function (data) {
                var url = filegroup_url + '/' + data.id;

                return '<a class="btn btn-xs btn-success"  href="' + url + '" data-placement="top" data-toggle="tooltip" title="View Details">'
                        + '<span class="fa fa-info-circle"></span>'
                        + '</a>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 2
        },
        {"render": function (data) {
                return '<button class="btn btn-danger btn-xs delete" data-placement="top" title="Delete File">'
                        + '<span class="glyphicon glyphicon-trash"></span>'
                        + '</button>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 3
        }
    ],
    "order": [[1, "desc"]]
});

$('#filegroup_tbl tbody').on('click', 'tr td .delete', function () {
    var rowNum = $(this).parents('tr')[0];
    var row = filegroup_tbl.row(rowNum);
    var rowData = row.data();
    $('#delete_file_name').text(rowData['name']);
    $('#del_btn').data('id', row.index());
    $('#confirm_delete').modal('toggle');
});

$(document).on('click', '#del_btn', function (e) {
    var rowNum = $('#del_btn').data('id');
    var row = filegroup_tbl.row(rowNum);
    var rowData = row.data();

    $.ajax({
        url: ws_filegroup_url + '/' + rowData['id'],
        type: 'DELETE',
        success: function () {
            row.remove().draw();
        }
    });
});

$('body').tooltip({selector: '[data-toggle="tooltip"]'});

$(document).ready(function () {
    $.ajax({
        url: encodeURI(ws_filegroup_url),
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            filegroup_tbl.clear();
            filegroup_tbl.rows.add(data);
            filegroup_tbl.draw();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert('Unable to fetch dataset.');
        }
    });
});
