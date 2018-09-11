$('#jobDetailForm').validate({
    rules: {
        name: {
            required: true
        }
    },
    messages: {
        name: {
            required: "Please enter a name for the job."
        }
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

$(document).ready(function () {
    $("#edit_info").click(function () {
        $("#view_job_description").hide();
        $("#edit_job_description").show();
        $('#name').focus();
    });
    $("#cancel_info").click(function () {
        $("#view_job_description").show();
        $("#edit_job_description").hide();
    });

    $('body').tooltip({selector: '[data-toggle="tooltip"]'});
});
