$('#passwordChange').validate({
    rules: {
        currentPassword: {
            minlength: 4,
            nowhitespace: true,
            required: true
        },
        newPassword: {
            minlength: 4,
            nowhitespace: true,
            required: true
        },
        newConfirmPassword: {
            equalTo: "#newPassword"
        }
    },
    messages: {
        currentPassword: {
            minlength: "Requires minimum 4 chars.",
            nowhitespace: "Space is not allowed.",
            required: "Please enter your password."
        },
        newPassword: {
            minlength: "Requires minimum 4 chars.",
            nowhitespace: "Space is not allowed.",
            required: "Please enter your password."
        },
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
