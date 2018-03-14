var varTypes = $('input:radio[name="varTypeId"]');
for (i = 1; i <= varTypes.length; i++) {
    $('select[id="selectOpt_' + i + '"]').bootstrapDualListbox();
}

varTypes.click(function () {
    var val = $(this).val();
    var selectOpt = $('#dataGroup_' + val);

    var id = 'selectOpt_' + val;
    if (document.getElementById(id).options.length === 0) {
        $.ajax({
            url: listUrl + val,
            dataType: 'json',
            type: 'GET',
            success: function (data) {
                var list = $('select[id="selectOpt_' + val + '"]');
                list.empty();
                $.each(data, function (index, value) {
                    var html = "<option value='" + value['id'] + "'>" + value['name'] + "</option>";
                    list.append(html);
                });
                list.bootstrapDualListbox('refresh', true);
            },
            error: function (xhr, ajaxOptions, thrownError) {
                alert('Unable to retrieve data list.');
            }
        });
    }

    $(".dataGroup").not(selectOpt).hide();
    selectOpt.show();
});

$(document).ready(function () {
    $('input:radio[name="varTypeId"]:checked').click();
});
