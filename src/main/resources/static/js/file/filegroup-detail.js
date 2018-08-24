function toHumanReadableSize(fileSize) {
    var unit = 1000;
    if (fileSize < unit) {
        return fileSize + ' B';
    }
    var size = ['kB', 'MB', 'GB', 'TB', 'PB', 'EB'];
    var exp = Math.floor(Math.log(fileSize) / Math.log(unit));

    return Number(fileSize / Math.pow(unit, exp)).toFixed(2) + ' ' + size[exp - 1];
}
$('#fileGroupForm').validate({
    rules: {
        groupName: {
            required: true
        }
    },
    messages: {
        groupName: "Please enter a name for the file group."
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    }
});

var var_types = $('input:radio[name="varTypeId"]');
for (i = 1; i <= var_types.length; i++) {
    $('#filegroup_' + i).bootstrapDualListbox();
}
$(".datagroup").hide();

var_types.click(function () {
    var val = $(this).val();
    var selectOpt = $('#datagroup_' + val);

    $(".datagroup").not(selectOpt).hide();
    selectOpt.show();
});

$(document).ready(function () {
    $("#edit_info").click(function () {
        $("#view_filegroup_detail").hide();
        $("#edit_filegroup_detail").show();
        $('#name').focus();
    });
    $("#cancel_info").click(function () {
        $("#view_filegroup_detail").show();
        $("#edit_filegroup_detail").hide();
    });

    $('input:radio[name="varTypeId"]:checked').click();

    $('#file_tbl').DataTable({
        pageLength: 10,
        responsive: true,
        "sAjaxSource": filegroup_url + '/' + filegroup_id + '/file',
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": "name"},
            {"mData": "fileSize"},
            {"mData": "creationTime"},
            {"mData": function (source) {
                    return {"id": source.id};
                }}
        ],
        "columnDefs": [
            {"render": function (data) {
                    return toHumanReadableSize(data);
                }, "type": 'file-size', "width": "10%", "targets": 1
            },
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "width": "10%", "targets": 2
            },
            {"render": function (data) {
                    var url = file_info_url + '/' + data.id;

                    return '<a class="btn btn-xs btn-success"  href="' + url + '" data-placement="top" data-toggle="tooltip" title="View Details">'
                            + '<span class="fa fa-info-circle"></span>'
                            + '</a>';
                }, "orderable": false, "bSearchable": false, "width": "0", "className": "text-center", "targets": 3
            }
        ],
        "order": [[2, "asc"]]
    });
});
