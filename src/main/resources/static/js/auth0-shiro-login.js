$(document).ready(function () {
    $('#loginForm').validate({
        rules: {
            email: {
                email: true,
                required: true
            },
            password: {
                minlength: 4,
                nowhitespace: true,
                required: true
            }
        },
        messages: {
            email: {
                email: "Please enter your email.",
                required: "E-mail is required."
            },
            password: {
                minlength: "Requires minimum 4 chars.",
                nowhitespace: "Space is not allowed.",
                required: "Please enter your password."
            }
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
