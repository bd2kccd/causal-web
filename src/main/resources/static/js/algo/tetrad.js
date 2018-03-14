var single_file_table = $('#single_file_table').DataTable();
var multiple_file_table = $('#multiple_file_table').DataTable();

function setSingleTable(listUrl) {
    single_file_table.clear();
    single_file_table.destroy();
    single_file_table = $('#single_file_table').DataTable({
        select: {
            style: 'single'
        },
        "sAjaxSource": listUrl,
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": "fileId"},
            {"mData": "title"},
            {"mData": "creationTime"},
            {"mData": "numOfCases"},
            {"mData": "numOfVars"}
        ],
        "columnDefs": [
            {
                "targets": 0,
                "visible": false,
                "searchable": false
            },
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "targets": 2
            }
        ],
        "order": [[1, "asc"]]
    });
}

function setMultipleTable(listUrl) {
    multiple_file_table.clear();
    multiple_file_table.destroy();
    multiple_file_table = $('#multiple_file_table').DataTable({
        select: {
            style: 'single'
        },
        "sAjaxSource": listUrl,
        "sAjaxDataProp": "",
        "aoColumns": [
            {"mData": "fileGroupId"},
            {"mData": "name"},
            {"mData": "creationTime"}
        ],
        "columnDefs": [
            {
                "targets": 0,
                "visible": false,
                "searchable": false
            },
            {"render": function (data) {
                    return moment(data).format('MMM DD, YYYY hh:mm:ss A');
                }, "targets": 2
            }
        ],
        "order": [[1, "asc"]]
    });
}
function listSelectAction(dt, rowIndex) {
    $('#set_param_btn').prop("disabled", false);
    $('input:hidden[name="datasetId"]').val(dt.cell(rowIndex, 0).data());
}
function listDeselectAction() {
    $('#set_param_btn').prop("disabled", true);
    $('#step2btn').attr('disabled', 'disabled');
    $('#step3btn').attr('disabled', 'disabled');
}
function fetchScores(algo, varType) {
    var list = $('select[id="score"]');
    $.ajax({
        url: urlPath + 'score/algo/' + algo + '/varType/' + varType,
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            list.empty();
            $.each(data, function (index, value) {
                var html = "<option value='" + value['value'] + "'>" + value['text'] + "</option>";
                list.append(html);
            });
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert('Unable to retrieve data list.');
        },
        complete: function () {
            list.prop("disabled", list.is(':empty'));
        }
    });
}
function fetchTests(algo, varType) {
    var list = $('select[id="test"]');
    $.ajax({
        url: urlPath + 'test/algo/' + algo + '/varType/' + varType,
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            list.empty();
            $.each(data, function (index, value) {
                var html = "<option value='" + value['value'] + "'>" + value['text'] + "</option>";
                list.append(html);
            });
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert('Unable to retrieve data list.');
        },
        complete: function () {
            list.prop("disabled", list.is(':empty'));
        }
    });
}
$(document).ready(function () {
    var dataType = $('ul#dataset_tab li.active a').attr('id');

    single_file_table.on('select', function (e, dt, type, indexes) {
        $('input:hidden[name="multiData"]').val(false);
        listSelectAction(dt, indexes);
    });
    single_file_table.on('deselect', function (e, dt, type, indexes) {
        listDeselectAction();
    });
    multiple_file_table.on('select', function (e, dt, type, indexes) {
        $('input:hidden[name="multiData"]').val(true);
        listSelectAction(dt, indexes);
    });
    multiple_file_table.on('deselect', function (e, dt, type, indexes) {
        listDeselectAction();
    });

    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        dataType = $(e.target).attr('id');
        $('input:radio[name="varTypeId"]:checked').click();
    });

    $('input:radio[name="varTypeId"]').click(function () {
        var listUrl = urlPath + 'data/' + dataType + '/' + $(this).val();
        switch (dataType) {
            case 'single':
                setSingleTable(listUrl);
                break;
            case 'multiple':
                setMultipleTable(listUrl);
                break;
        }

        $('#set_param_btn').prop("disabled", true);
        $('#step2btn').attr('disabled', 'disabled');
        $('#step3btn').attr('disabled', 'disabled');
    });


    $("#algorithm").change(function () {
        var algo = $("#algorithm :selected").attr('value');
        var varType = $('input[name=varTypeId]:checked').val();
        fetchScores(algo, varType);
        fetchTests(algo, varType);
    });

    $('input:radio[name="varTypeId"]:checked').click();
    $("#algorithm").trigger("change");
});