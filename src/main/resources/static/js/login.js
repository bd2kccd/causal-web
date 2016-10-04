$(document).ready(function () {
    $('#login').validate({
        rules: {
            loginUsername: {
                email: true,
                required: true
            },
            loginPassword: {
                required: true
            }
        },
        messages: {
            loginUsername: {
                email: "Please enter a valid email.",
                required: "E-mail is required."
            },
            loginPassword: "Please enter your password."
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
    $('#pwd_reset').validate({
        rules: {
            usernameRecover: {
                email: true,
                required: true
            }
        },
        messages: {
            usernameRecover: {
                email: "Please enter a valid email.",
                required: "Email is required."
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
    $('#registration').validate({
        rules: {
            username: {
                email: true,
                required: true
            },
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
            username: "Please enter a valid email.",
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
    $('#register').prop('disabled', !$('#agree').prop('checked'));

    $('#agree').click(function () {
        $('#register').prop('disabled', !$(this).prop('checked'));
    })
});
