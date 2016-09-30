$(document).ready(function () {
    $("#algoForm").validate({
        rules: {
            dataset: {
                required: true
            },
            penaltyDiscount: {
                required: true,
                number: true,
                greaterThan: 0
            },
            maxDegree: {
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
            penaltyDiscount: {
                required: "Please set the penalty discount.",
                greaterThan: "Value must be greater than 0."
            },
            maxDegree: {
                required: "Please select the search max degree."
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
