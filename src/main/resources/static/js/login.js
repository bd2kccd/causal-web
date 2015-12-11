$.validator.addMethod("nowhitespace", function (value, element) {
    return this.optional(element) || /^\S+$/i.test(value);
}, "No white space please");

$(document).ready(function () {
    $('#login').validate({
        rules: {
            username: {
                minlength: 3,
                nowhitespace: true,
                required: true
            },
            password: {
                minlength: 5,
                maxlength: 25,
                nowhitespace: true,
                required: true
            }
        },
        messages: {
            username: "Please enter a valid username.",
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
    $('#registration').validate({
        rules: {
            username: {
                minlength: 3,
                nowhitespace: true,
                required: true
            },
            email: {
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
            username: "Please enter a valid username (no white space).",
            email: "Please enter a valid email.",
            password: "Please enter valid a password (5-25 chars).",
            confirmPassword: "Please reenter password."
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