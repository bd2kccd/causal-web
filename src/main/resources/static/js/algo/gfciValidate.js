$(document).ready(function () {
    $("#algoForm").validate({
        rules: {
            dataset: {
                required: true
            },
            alpha: {
                required: true,
                number: true,
                min: 0
            },
            penaltyDiscount: {
                required: true,
                number: true,
                greaterThan: 0
            },
            maxInDegree: {
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
            alpha: {
                required: "Please set the alpha.",
                greaterThan: "Value must be greater than 0."
            },
            penaltyDiscount: {
                required: "Please set the penalty discount.",
                greaterThan: "Value must be greater than 0."
            },
            maxInDegree: {
                required: "Please select the search maximum in-degree."
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
