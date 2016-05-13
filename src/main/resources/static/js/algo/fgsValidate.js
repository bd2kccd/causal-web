$(document).ready(function () {
    $("#algoForm").validate({
        rules: {
            dataset: {
                required: true
            },
            dataType: {
                required: true
            },
            delimiter: {
                required: true
            },
            penaltyDiscount: {
                required: true,
                number: true,
                min: 0
            },
            depth: {
                required: true,
                number: true,
                min: -1
            },
            jvmMaxMem: {
                required: true,
                number: true,
                min: 0,
                max: 256
            }
        },
        messages: {
            dataset: {
                required: "Please select a dataset."
            },
            dataType: {
                required: "Please select a data type."
            },
            delimiter: {
                required: "Please select a delimiter."
            },
            penaltyDiscount: {
                required: "Please set the penalty discount."
            },
            depth: {
                required: "Please select the search depth."
            },
            jvmMaxMem: {
                required: "Must be a number between 0 and 256.",
                min: "Must between 0 and 256.",
                max: "Must between 0 and 256."
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
