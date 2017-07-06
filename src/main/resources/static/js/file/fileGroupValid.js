$('#fileGroupForm').validate({
    rules: {
        groupName: {
            required: true
        },
        continuousFileIds: {
            required: true,
            depends: function (element) {
                return $("input[name='fileVariableTypeId']:checked").val() == 1;
            }
        },
        discreteFileIds: {
            required: true,
            depends: function (element) {
                return $("input[name='fileVariableTypeId']:checked").val() == 2;
            }
        },
        mixedFileIds: {
            required: true,
            depends: function (element) {
                return $("input[name='fileVariableTypeId']:checked").val() == 3;
            }
        }
    },
    messages: {
        groupName: "Please enter a name for the file group.",
        continuousFileIds: "Please select at least one file.",
        discreteFileIds: "Please select at least one file.",
        mixedFileIds: "Please select at least one file."
    },
    highlight: function (element) {
        $(element).closest('.form-group').addClass('has-error');
    },
    unhighlight: function (element) {
        $(element).closest('.form-group').removeClass('has-error');
    }
});