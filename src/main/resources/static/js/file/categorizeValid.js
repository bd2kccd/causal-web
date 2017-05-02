$('#fileInfoUpdate').validate({
    rules: {
        title: {
            required: true
        }
    },
    messages: {
        title: "File title is required."
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    }
});
