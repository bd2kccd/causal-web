$('#fileGroupForm').validate({
    rules: {
        groupName: {
            required: true
        }
    },
    messages: {
        groupName: "Please enter a name for the file group."
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    }
});
