$('#fileGroupForm').validate({
    rules: {
        name: {
            required: true
        }
    },
    messages: {
        name: "Please enter a name for the file group."
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
    $('input:radio[name="varTypeId"]:checked').click();
});
