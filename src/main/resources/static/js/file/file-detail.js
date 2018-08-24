$('#fileDetailForm').validate({
    rules: {
        name: {
            required: true
        }
    },
    messages: {
        name: {
            required: "Please enter a name for the file."
        }
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    },
    errorElement: 'span',
    errorClass: 'help-block'
});

$(document).ready(function () {
    $("#edit_info").click(function () {
        $("#view_file_detail").hide();
        $("#edit_file_detail").show();
        $('#name').focus();
    });
    $("#cancel_info").click(function () {
        $("#view_file_detail").show();
        $("#edit_file_detail").hide();
    });
});
