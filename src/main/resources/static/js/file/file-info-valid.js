$('#categorizeFile').validate({
    rules: {
        fileFormatId: {
            required: true
        },
        fileDelimiterId: {
            required: true
        },
        variableTypeId: {
            required: true
        }
    },
    messages: {
        fileTypeId: "Please select a file format.",
        fileDelimiterId: "Please select a file delimiter.",
        variableTypeId: "Please select a variable type."
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    }
});
