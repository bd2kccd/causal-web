$(document).ready(function () {
    $('#password_reset').validate({
        rules: {
            password: {
                minlength: 4,
                maxlength: 10,
                nowhitespace: true,
                required: true
            },
            confirmPassword: {
                equalTo: "#password"
            }
        },
        messages: {
            password: {
                required: "Please enter a new password.",
                nowhitespace: "Space is not allowed.",
                minlength: "Requires minimum 4 chars.",
                maxlength: "Number of chars exceed."
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