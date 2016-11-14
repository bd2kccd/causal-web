$(document).ready(function () {
    $('#password_reset').validate({
        rules: {
            password: {
                minlength: 5,
                maxlength: 25,
                nowhitespace: true,
                required: true
            },
            confirmPassword: {
                equalTo: "#password"
            }
        },
        messages: {
            password: "Please enter valid a password (5-25 chars).",
            confirmPassword: "Please reenter password.",
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
