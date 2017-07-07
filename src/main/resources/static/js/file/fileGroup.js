var varTypes = $('input:radio[name="varTypeId"]');
for (i = 1; i <= varTypes.length; i++) {
    $('select[id="selectOpt_' + i + '"]').bootstrapDualListbox();
}

varTypes.click(function () {
    var val = $(this).val();
    var selectOpt = $('#dataGroup_' + val);

    $(".dataGroup").not(selectOpt).hide();
    selectOpt.show();
});

$(document).ready(function () {
    $('input:radio[name="varTypeId"]:checked').click();
});
