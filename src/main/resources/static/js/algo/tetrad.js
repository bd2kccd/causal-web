var datatable;
var datatype = '';
var vartype = '';
var step_btn = 'step1btn';

function updateTable() {
    var uri = url_path + 'data/' + datatype + '/' + vartype;
    $.ajax({
        url: encodeURI(uri),
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            datatable.clear();
            datatable.rows.add(data);
            datatable.draw();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert('Unable to fetch dataset.');
        }
    });
    disableButtonsAtStep1();
}
function listSelectAction(dt, rowIndex) {
    $('#step1NextBtn').prop("disabled", false);
    $('input:hidden[name="datasetId"]').val(dt.cell(rowIndex, 0).data());

    // update summary table
    $('.dataset_name').html(dt.cell(rowIndex, 1).data());
    $('#data_type').html(datatype.toLowerCase());
    $('#var_type').html($('input:radio[name="varTypeId"]:checked').next().text().toLowerCase());
}
function disableButtonsAtStep1() {
    $('#step1NextBtn').prop("disabled", true);
    $('#step2btn').prop("disabled", true);
    $('#step3btn').prop("disabled", true);
    $('#step4btn').prop("disabled", true);
}
function disableButtonsAtStep2() {
    $('#step2NextBtn').prop("disabled", true);
    $('#step3btn').prop("disabled", true);
    $('#step4btn').prop("disabled", true);
}
function fetchParameters() {
    var algo = $("#algorithm :selected").val();
    var score = $("#score :selected").val();
    var test = $("#test :selected").val();

    var uri = url_path + 'param/algo/' + algo;
    var params = {};
    if (score !== null) {
        params['score'] = score;
    }
    if (test !== null) {
        params['test'] = test;
    }
    uri = uri + '?' + $.param(params);
    $.ajax({
        url: uri,
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            var params = $('#params');
            $('#params > .form-group').remove();
            var htmlParams = '';
            $.each(data, function (index, value) {
                var txt = value['text'];
                var id = value['value'];
                var minVal = value['minVal'];
                var maxVal = value['maxVal'];
                var val = value['defaultVal'];
                var isNumeric = value['numeric'];
                var isBoolean = value['aBoolean'];
                var input_attr = 'id="' + id + '" name="' + id + '"';

                var angularjs_attr;
                if (isNumeric || isBoolean) {
                    angularjs_attr = 'data-ng-model="' + id + '" data-ng-init="' + id + '=' + val + '"';
                } else {
                    angularjs_attr = 'data-ng-model="' + id + '" data-ng-init="' + id + '=\'' + val + '\'"';
                }

                var form;
                if (isNumeric) {
                    var label = '<label for="' + id + '" class="control-label">' + txt + '</label>';
                    var input = '<input type="number" class="form-control" min="' + minVal + '" max="' + maxVal + '" ' + input_attr + angularjs_attr + ' value="' + val + '" required="required" />';
                    form = '<div class="form-group">' + label + input + '</div>';
                } else if (isBoolean) {
                    var label = '<div class="checkbox"><label for="' + id + '" class="control-label">';
                    var chkbx;
                    if (val) {
                        chkbx = '<input type="checkbox" ' + input_attr + angularjs_attr + ' checked="checked" />' + txt;
                    } else {
                        chkbx = '<input type="checkbox" ' + input_attr + angularjs_attr + ' />' + txt;
                    }
                    var endLabel = '</label></div>';
                    form = '<div class="form-group">' + label + chkbx + endLabel + '</div>';
                } else {
                    var label = '<label>' + txt + '</label>';
                    var input = '<input class="form-control" ' + input_attr + angularjs_attr + ' value="' + val + '" />';
                    form = '<div class="form-group">' + label + input + '</div>';
                }
                htmlParams += form;
            });
            $(params).append(htmlParams);

            // update summary table
            var params_tbody = $('#params_tbl > tbody');
            $(params_tbody).empty();
            var param_html;
            $.each(data, function (index, value) {
                var txt = value['text'];
                var id = value['value'];
                param_html += '<tr><td>' + txt + ':</td><td data-ng-bind="' + id + '" id="param_' + id + '"></td></tr>';
            });
            $(params_tbody).append(param_html);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText);
        }
    });

    // update summary table
    $('.algo_name').html($("#algorithm :selected").text().toLowerCase());
    $('.score_name').html($("#score :selected").text().toLowerCase());
    $('.test_name').html($("#test :selected").text().toLowerCase());
}
function fetchTests(algoName, varTypeId) {
    if (!algoName) {
        return;
    }

    var list = $('#test');
    $.ajax({
        url: url_path + 'test/algo/' + algoName + '/varType/' + varTypeId,
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            list.empty();
            $.each(data.options, function (index, value) {
                var html = "<option value='" + value['value'] + "'>" + value['text'] + "</option>";
                list.append(html);
            });

            if (list.is(':empty')) {
                list.prop("disabled", true);
            } else {
                list.val(data.defaultValue).change();
                list.prop("disabled", false);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText);
        }
    });
}
function fetchScores(algoName, varTypeId) {
    if (!algoName) {
        return;
    }

    var list = $('#score');
    $.ajax({
        url: url_path + 'score/algo/' + algoName + '/varType/' + varTypeId,
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            list.empty();
            $.each(data.options, function (index, value) {
                var html = "<option value='" + value['value'] + "'>" + value['text'] + "</option>";
                list.append(html);
            });

            if (list.is(':empty')) {
                list.prop("disabled", true);
            } else {
                list.val(data.defaultValue).change();
                list.prop("disabled", false);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText);
        }
    });
}
function fetchDescription(algoName) {
    if (!algoName) {
        return;
    }

    $.ajax({
        url: url_path + 'description/algo/' + algoName,
        dataType: 'text',
        type: 'GET',
        success: function (data) {
            $('#description').text(data);
        }
    });
}
function fetchAlgorithms(algoType) {
    var list = $('#algorithm');
    $.ajax({
        url: url_path + 'algo/' + algoType + '/' + datatype,
        dataType: 'json',
        type: 'GET',
        success: function (data) {
            list.empty();
            $.each(data, function (index, value) {
                var html = "<option value='" + value['value'] + "'>" + value['text'] + "</option>";
                list.append(html);
            });
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText);
            disableButtonsAtStep2();
        },
        complete: function () {
            if (list.is(':empty')) {
                list.prop("disabled", true);
                $('#description').text('');
                disableButtonsAtStep2();

                // clear scores
                var scores = $('#score');
                scores.empty();
                scores.prop("disabled", true);

                // clear tests
                var tests = $('#test');
                scores.empty();
                tests.prop("disabled", true);
            } else {
                list.prop("disabled", false);
                $('#step2NextBtn').prop("disabled", false);
                $('#algorithm option:first').attr('selected', 'selected');
                list.trigger('change');
            }
        }
    });
}
function updateParamSummary() {
    $("#params").find("input").each(function () {
        var id = '#param_' + $(this).attr('data-ng-model');
        var val = $(this).val();
        if ($(this).attr('type') === 'checkbox') {
            $(id).text($(this).prop('checked') ? 'yes' : 'no');
        } else {
            $(id).text(val);
        }
    });
}
$(document).ready(function () {
    var vartype_radio = $('input:radio[name="varTypeId"]');
    var datatype_tab = $('a[data-toggle="tab"]');
    var single_datatable = $('#single_datatable').DataTable({
        select: {
            style: 'single'
        },
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
    var multi_datatable = $('#multi_datatable').DataTable({
        select: {
            style: 'single'
        },
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
    single_datatable.on('select', function (e, dt, type, indexes) {
        listSelectAction(dt, indexes);
    });
    single_datatable.on('deselect', function (e, dt, type, indexes) {
        disableButtonsAtStep1();
    });
    multi_datatable.on('select', function (e, dt, type, indexes) {
        listSelectAction(dt, indexes);
    });
    multi_datatable.on('deselect', function (e, dt, type, indexes) {
        disableButtonsAtStep1();
    });

    $(datatype_tab).click(function () {
        var curr_datatype = $(this).attr('id');
        if (curr_datatype !== datatype) {
            datatype = curr_datatype;
            switch (datatype) {
                case 'single':
                    datatable = single_datatable;
                    $('input:hidden[name="singleFile"]').val(true);
                    break;
                case 'multiple':
                    datatable = multi_datatable;
                    $('input:hidden[name="singleFile"]').val(false);
                    break;
            }
            updateTable();
        }
    });
    $(vartype_radio).click(function () {
        var curr_vartype = $(this).val();
        if (curr_vartype !== vartype) {
            vartype = curr_vartype;
            updateTable();
        }
    });
    $('input:radio[name="algoType"]').click(function () {
        fetchAlgorithms($(this).val());
    });
    $("#algorithm").change(function () {
        var algoName = $("#algorithm :selected").val();
        var varTypeId = $('input[name=varTypeId]:checked').val();
        fetchScores(algoName, varTypeId);
        fetchTests(algoName, varTypeId);
        fetchDescription(algoName);

        $('#step4btn').prop("disabled", true);
    });
    $("#score").change(function () {
        $('#step4btn').prop("disabled", true);
    });
    $("#test").change(function () {
        $('#step4btn').prop("disabled", true);
    });
    $('#step1btn').click(function (e) {
        step_btn = $(this).attr('id');
    });
    $('#step2btn').click(function (e) {
        if (step_btn === 'step1btn') {
            $("#algorithm").trigger('change');
        }
        step_btn = $(this).attr('id');
        $('input:radio[name="algoType"]:checked').click();
    });
    $('#step3btn').click(function (e) {
        if (step_btn === 'step2btn') {
            fetchParameters();
        }
        step_btn = $(this).attr('id');
    });
    $('#step4btn').click(function (e) {
        if (step_btn === 'step4btn') {
            return;
        }
        updateParamSummary();
        step_btn = $(this).attr('id');
    });

    datatable = single_datatable;
    datatype = $(datatype_tab).attr('id');
    vartype = $(vartype_radio).filter(':checked').val();
    $('input:radio[name="algoType"]:checked').click();
    $('input:hidden[name="singleFile"]').val(true);

    updateTable();
    disableButtonsAtStep1();
});
