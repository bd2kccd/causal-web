$(document).ready(function () {
    $('#password_reset').validate({
        rules: {
            password: {
                minlength: 4,
                nowhitespace: true,
                required: true
            },
            confirmPassword: {
                equalTo: "#password"
            }
        },
        messages: {
            password: {
                minlength: "Requires minimum 4 chars.",
                nowhitespace: "Space is not allowed.",
                required: "Please enter your password."
            },
            confirmPassword: "Please re-enter the new password."
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        unhighlight: function (element) {
            $(element).closest('.form-group').removeClass('has-error');
        },
        errorElement: 'span',
        errorClass: 'help-block',
        errorPlacement: function (error, element) {
            if (element.parent('.input-group').length) {
                error.insertAfter(element.parent());
            } else {
                error.insertAfter(element);
            }
        }
    });
});
