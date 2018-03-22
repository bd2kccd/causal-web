var single_data_tbl = $('#single_data_tbl').DataTable();
var multi_data_tbl = $('#multi_data_tbl').DataTable();
var data_type = 'single';

function setSingleTable(listUrl) {
    single_data_tbl.clear();
    single_data_tbl.destroy();
    single_data_tbl = $('#single_data_tbl').DataTable({
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
    multi_data_tbl.clear();
    multi_data_tbl.destroy();
    multi_data_tbl = $('#multi_data_tbl').DataTable({
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
    $('#step1NextBtn').prop("disabled", false);
    $('input:hidden[name="datasetId"]').val(dt.cell(rowIndex, 0).data());
}

function setSelectedDataType(type) {
    data_type = type;
    $('input:hidden[name="multiData"]').val(data_type === 'multiple');
}

function disableButtonsAtStep1() {
    $('#step1NextBtn').prop("disabled", true);
    $('#step2btn').prop("disabled", true);
    $('#step3btn').prop("disabled", true);
    $('#step4btn').prop("disabled", true);
}

function fetchScores(algoName, varTypeId) {
    var list = $('select[id="score"]');
    $.ajax({
        url: url_path + 'score/algo/' + algoName + '/varType/' + varTypeId,
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
            if (list.is(':empty')) {
                list.prop("disabled", true);
            } else {
                list.prop("disabled", false);
                $.ajax({
                    url: url_path + 'score/default/varType/' + varTypeId,
                    dataType: 'json',
                    type: 'GET',
                    success: function (data) {
                        list.val(data['value']).change();
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        $('#score option:first').attr('selected', 'selected');
                    }
                });
            }
        }
    });
}
function fetchTests(algoName, varTypeId) {
    var list = $('select[id="test"]');
    $.ajax({
        url: url_path + 'test/algo/' + algoName + '/varType/' + varTypeId,
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
            if (list.is(':empty')) {
                list.prop("disabled", true);
            } else {
                list.prop("disabled", false);
                $.ajax({
                    url: url_path + 'test/default/varType/' + varTypeId,
                    dataType: 'json',
                    type: 'GET',
                    success: function (data) {
                        list.val(data['value']).change();
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        $('#test option:first').attr('selected', 'selected');
                    }
                });
            }
        }
    });
}

$(document).ready(function () {
    single_data_tbl.on('select', function (e, dt, type, indexes) {
        listSelectAction(dt, indexes);
    });
    single_data_tbl.on('deselect', function (e, dt, type, indexes) {
        disableButtonsAtStep1();
    });
    multi_data_tbl.on('select', function (e, dt, type, indexes) {
        listSelectAction(dt, indexes);
    });
    multi_data_tbl.on('deselect', function (e, dt, type, indexes) {
        disableButtonsAtStep1();
    });
    $('a[data-toggle="tab"]').click(function () {
        setSelectedDataType($(this).attr('id'));
        $('input:radio[name="varTypeId"]:checked').click();
    });
    $('input:radio[name="varTypeId"]').click(function () {
        var list_url = url_path + 'data/' + data_type + '/' + $(this).val();
        switch (data_type) {
            case 'single':
                setSingleTable(list_url);
                break;
            case 'multiple':
                setMultipleTable(list_url);
                break;
        }
        disableButtonsAtStep1();
    });
    $('input:radio[name="algoType"]').click(function () {
        var list = $('#algorithm');
        $.ajax({
            url: url_path + 'algo/' + $(this).val(),
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
                if (list.is(':empty')) {
                    list.prop("disabled", true);
                } else {
                    list.prop("disabled", false);
                    $('#algorithm option:first').attr('selected', 'selected');
                    list.trigger('change');
                }
            }
        });
    });

    $("#algorithm").change(function () {
        var algoName = $("#algorithm :selected").val();
        var varTypeId = $('input[name=varTypeId]:checked').val();
        fetchScores(algoName, varTypeId);
        fetchTests(algoName, varTypeId);
    });

    // init
    setSelectedDataType($('#dataset_tab li.active a').attr('id'));
    $('input:radio[name="varTypeId"]:checked').click();
    $('input:radio[name="algoType"]:checked').click();
});