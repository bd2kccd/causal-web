$(document).ready(function () {
    $("#algoForm").validate({
        rules: {
            dataset: {
                required: true
            },
            structurePrior: {
                required: true,
                number: true,
                greaterThan: 0
            },
            samplePrior: {
                required: true,
                number: true,
                greaterThan: 0
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
                max: 128
            }
        },
        messages: {
            dataset: {
                required: "Please select a dataset."
            },
            structurePrior: {
                required: "Please set the structure prior.",
                greaterThan: "Value must be greater than 0."
            },
            samplePrior: {
                required: "Please set the sample prior.",
                greaterThan: "Value must be greater than 0."
            },
            depth: {
                required: "Please select the search depth."
            },
            jvmMaxMem: {
                required: "Must be a number between 0 and 128.",
                min: "Must between 0 and 128.",
                max: "Must between 0 and 128."
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
