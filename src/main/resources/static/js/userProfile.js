$(document).ready(function () {
    $("#view_info_panel").show();
    $("#edit_info_panel").hide();

    $("#edit_info").click(function () {
        $("#view_info_panel").hide();
        $("#edit_info_panel").show();
    });
    $("#cancel_info").click(function () {
        $("#view_info_panel").show();
        $("#edit_info_panel").hide();
    });

    $("#view_workspace_panel").show();
    $("#edit_workspace_panel").hide();

    $("#edit_workspace").click(function () {
        $("#view_workspace_panel").hide();
        $("#edit_workspace_panel").show();
    });
    $("#cancel_workspace").click(function () {
        $("#view_workspace_panel").show();
        $("#edit_workspace_panel").hide();
    });

    $("#view_usqa_panel").show();
    $("#edit_usqa_panel").hide();

    $("#edit_usqa").click(function () {
        $("#view_usqa_panel").hide();
        $("#edit_usqa_panel").show();
    });
    $("#cancel_usqa").click(function () {
        $("#view_usqa_panel").show();
        $("#edit_usqa_panel").hide();
    });
});

$.validator.addMethod("nowhitespace", function (value, element) {
    return this.optional(element) || /^\S+$/i.test(value);
}, "No white space please");

$('#passwordChange').validate({
    rules: {
        currentPassword: {
            minlength: 5,
            maxlength: 25,
            nowhitespace: true,
            required: true
        },
        newPassword: {
            minlength: 5,
            maxlength: 25,
            nowhitespace: true,
            required: true
        },
        confirmPassword: {
            equalTo: "#newPassword"
        }
    },
    messages: {
        currentPassword: "Please enter valid a password (5-25 chars).  No space",
        newPassword: "Please enter valid a password (5-25 chars). No space",
        confirmPassword: "Does not match new password."
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