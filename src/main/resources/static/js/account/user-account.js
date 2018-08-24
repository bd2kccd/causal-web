$(document).ready(function () {
    $("#edit_profile").click(function () {
        $("#view_profile_panel").hide();
        $("#edit_profile_panel").show();
    });
    $("#cancel_edit_profile").click(function () {
        $("#view_profile_panel").show();
        $("#edit_profile_panel").hide();
    });

    $('#password_reset_form').validate({
        rules: {
            currentPassword: {
                minlength: 4,
                maxlength: 10,
                nowhitespace: true,
                required: true
            },
            newPassword: {
                minlength: 4,
                maxlength: 10,
                nowhitespace: true,
                required: true
            },
            newConfirmPassword: {
                equalTo: "#newPassword"
            }
        },
        messages: {
            currentPassword: "Please enter your current password.",
            newPassword: "Please enter valid a password (4-10 chars).",
            newConfirmPassword: "Password does not match."
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
});
            