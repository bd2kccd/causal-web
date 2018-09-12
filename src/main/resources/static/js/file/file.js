function toHumanReadableSize(fileSize) {
    var unit = 1000;
    if (fileSize < unit) {
        return fileSize + ' B';
    }
    var size = ['kB', 'MB', 'GB', 'TB', 'PB', 'EB'];
    var exp = Math.floor(Math.log(fileSize) / Math.log(unit));
    return Number(fileSize / Math.pow(unit, exp)).toFixed(2) + ' ' + size[exp - 1];
}

var specific_file_tbl = $('#specific_file_tbl').DataTable({
    pageLength: 10,
    responsive: true,
    "sAjaxDataProp": "",
    "aoColumns": [
        {"mData": "name"},
        {"mData": "size"},
        {"mData": "creationTime"},
        {"mData": function (source) {
                return {"id": source.id};
            }},
        {"mData": function (source) {
                return {"id": source.id, "categorize": source.categorized};
            }},
        {"mData": function (source) {
                return {"id": source.id};
            }}
    ],
    "columnDefs": [
        {"render": function (data) {
                return toHumanReadableSize(data);
            }, "type": 'file-size', "width": "0", "className": "text-center", "targets": 1
        },
        {"render": function (data) {
                return moment(data).format('MMM DD, YYYY hh:mm:ss A');
            }, "width": "0", "className": "text-center", "targets": 2
        },
        {"render": function (data) {
                var url = file_info_url + data.id;

                return '<a class="btn btn-xs btn-success"  href="' + url + '" data-placement="top" data-toggle="tooltip" title="View Details">'
                        + '<span class="fa fa-info-circle"></span>'
                        + '</a>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 3
        },
        {"render": function (data) {
                var url = file_info_url + 'categorization/' + data.id;
                var btn = data.categorize ? 'btn btn-xs btn-info' : 'btn btn-xs btn-warning';
                var title = data.categorize ? 'Recategorize File' : 'Categorize File';

                return '<a class="' + btn + '"  href="' + url + '" data-placement="top" data-toggle="tooltip" title="' + title + '">'
                        + '<span class="glyphicon glyphicon-file"></span>'
                        + '</a>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 4
        },
        {"render": function (data) {
                return '<button class="btn btn-danger btn-xs delete" data-placement="top" title="Delete File">'
                        + '<span class="glyphicon glyphicon-trash"></span>'
                        + '</button>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 5
        }
    ],
    "rowCallback": function (row, data, index) {
        if (!data.categorized) {
            $(row).addClass('warning');
        }
    },
    "order": [[2, "asc"]]
});

var all_file_tbl = $('#all_file_tbl').DataTable({
    pageLength: 10,
    responsive: true,
    "sAjaxDataProp": "",
    "aoColumns": [
        {"mData": "name"},
        {"mData": "size"},
        {"mData": "creationTime"},
        {"mData": "type"},
        {"mData": function (source) {
                return {"id": source.id};
            }},
        {"mData": function (source) {
                return {"id": source.id, "categorize": source.categorized};
            }},
        {"mData": function (source) {
                return {"id": source.id};
            }}
    ],
    "columnDefs": [
        {"render": function (data) {
                return toHumanReadableSize(data);
            }, "type": 'file-size', "width": "0", "className": "text-center", "targets": 1
        },
        {"render": function (data) {
                return moment(data).format('MMM DD, YYYY hh:mm:ss A');
            }, "width": "0", "className": "text-center", "targets": 2
        },
        {
            "width": "0", "className": "text-center", "targets": 3
        },
        {"render": function (data) {
                var url = file_info_url + data.id;

                return '<a class="btn btn-xs btn-success"  href="' + url + '" data-placement="top" data-toggle="tooltip" title="View Details">'
                        + '<span class="fa fa-info-circle"></span>'
                        + '</a>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 4
        },
        {"render": function (data) {
                var url = file_info_url + 'categorization/' + data.id;
                var btn = data.categorize ? 'btn btn-xs btn-info' : 'btn btn-xs btn-warning';
                var title = data.categorize ? 'Recategorize File' : 'Categorize File';

                return '<a class="' + btn + '"  href="' + url + '" data-placement="top" data-toggle="tooltip" title="' + title + '">'
                        + '<span class="glyphicon glyphicon-file"></span>'
                        + '</a>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 5
        },
        {"render": function (data) {
                return '<button class="btn btn-danger btn-xs delete" data-placement="top" title="Delete File">'
                        + '<span class="glyphicon glyphicon-trash"></span>'
                        + '</button>';
            }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 6
        }
    ],
    "rowCallback": function (row, data, index) {
        if (!data.categorized) {
            $(row).addClass('warning');
        }
    },
    "order": [[2, "asc"]]
});

var file_tbl = all_file_tbl;

function updateList(id) {
    var uri = ws_file_url + id;
    $.ajax({
        url: encodeURI(uri),
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            file_tbl.clear();
            file_tbl.rows.add(data);
            file_tbl.draw();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert('Unable to fetch dataset.');
        }
    });
}

$('#all_file_tbl tbody').on('click', 'tr td .delete', function () {
    var rowNum = $(this).parents('tr')[0];
    var row = file_tbl.row(rowNum);
    var rowData = row.data();
    $('#delete_file_name').text(rowData['name']);
    $('#del_btn').data('id', row.index());
    $('#confirm_delete').modal('toggle');
});
$('#specific_file_tbl tbody').on('click', 'tr td .delete', function () {
    var rowNum = $(this).parents('tr')[0];
    var row = file_tbl.row(rowNum);
    var rowData = row.data();
    $('#delete_file_name').text(rowData['name']);
    $('#del_btn').data('id', row.index());
    $('#confirm_delete').modal('toggle');
});


$(document).on('click', '#del_btn', function (e) {
    var rowNum = $('#del_btn').data('id');
    var row = file_tbl.row(rowNum);
    var rowData = row.data();

    var uri = ws_file_url + rowData['id'];
    $.ajax({
        url: encodeURI(uri),
        type: 'DELETE'
    }).done(function () {
        row.remove().draw();
        $('#err_msg_txt').text('');
        $('#err_msg').css({'display': 'none'});
    }).fail(function (jqXHR, textStatus, errorThrown) {
        $('#err_msg_txt').text(jqXHR.responseText);
        $('#err_msg').css({'display': 'block'});
    });
});

$(document).ready(function () {
    $(".nav-pills a").click(function () {
        var id = $(this).attr('id');
        if (id === '-1') {
            $("#list_specific").hide();
            $("#list_all").show();
            file_tbl = all_file_tbl;
        } else {
            $("#list_all").hide();
            $("#list_specific").show();
            file_tbl = specific_file_tbl;
        }
        updateList(id);
    });
    $('body').tooltip({selector: '[data-toggle="tooltip"]'});
    $('#' + defaultFileFmt).click();
});
