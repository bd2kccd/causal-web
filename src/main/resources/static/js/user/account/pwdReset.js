$.validator.addMethod("nowhitespace", function (value, element) {
    return this.optional(element) || /^\S+$/i.test(value);
}, "No white space please");

$(document).ready(function () {
    $('#resetPwdForm').validate({
        rules: {
            username: {
                minlength: 4,
                nowhitespace: true,
                required: true
            },
            answer: {
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
            username: "Please enter a valid username.",
            answer: "Please provide answer to the security question.",
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
});
