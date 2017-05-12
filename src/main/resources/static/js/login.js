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
            password: "Please enter your password."
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
            emailRecoverFrom: {
                email: true,
                required: true
            }
        },
        messages: {
            emailRecoverFrom: {
                email: "Please enter your email.",
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
            registerEmail: {
                email: true,
                required: true
            },
            registerPassword: {
                minlength: 4,
                maxlength: 10,
                nowhitespace: true,
                required: true
            },
            confirmRegisterPassword: {
                equalTo: "#registerPassword"
            }
        },
        messages: {
            registerEmail: "Please enter your email.",
            registerPassword: "Please enter a password (4-10 chars).",
            confirmRegisterPassword: "Please re-enter your password.",
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