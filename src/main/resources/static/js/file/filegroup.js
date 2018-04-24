$(document).ready(function () {
    $('input:radio[name="varTypeId"]').click(function () {
        var val = $(this).val();
        var selectOpt = $('#datagroup_' + val);

        $(".datagroup").not(selectOpt).hide();
        selectOpt.show();
    });
    $('input:radio[name="varTypeId"]:checked').click();
});
