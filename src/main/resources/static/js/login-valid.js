$(document).ready(function () {
    $('#login').validate({
        rules: {
            email: {
                email: true,
                required: true
            },
            password: {
                minlength: 4,
                maxlength: 10,
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
                required: "Please enter your password.",
                nowhitespace: "Space is not allowed.",
                minlength: "Requires minimum 4 chars.",
                maxlength: "Number of chars exceed."
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