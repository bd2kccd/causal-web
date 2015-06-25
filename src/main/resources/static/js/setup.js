$(document).ready(function () {
    $("#userSetup").validate({
        rules: {
            firstName: {
                minlength: 2,
                required: true
            },
            lastName: {
                minlength: 2,
                required: true
            },
            email: {
                email: true,
                required: true
            },
            workspaceDirectory: {
                minlength: 1,
                required: true
            }
        },
        messages: {
            firstName: {
                minlength: "At least 2 characters required!",
                required: "Please enter your first name."
            },
            lastName: {
                minlength: "At least 2 characters required!",
                required: "Please enter your last name."
            },
            email: "Please enter a valid email.",
            workspaceDirectory: {
                minlength: "At least 1 characters required!",
                required: "Please enter an existing directory for the workspace."
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
