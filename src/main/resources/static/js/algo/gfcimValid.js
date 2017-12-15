$(document).ready(function () {
    $("#algoForm").validate({
        rules: {
            dataset: {
                required: true
            },
            jvmMaxMem: {
                required: true,
                number: true,
                min: 0,
                max: 128
            },
            alpha: {
                required: true,
                number: true,
                min: 0.0,
                max: 1.0
            },
            structurePrior: {
                required: true,
                number: true,
                min: 1
            },
            maxDegree: {
                required: true,
                number: true,
                min: -1
            },
            maxPathLength: {
                required: true,
                number: true,
                min: -1
            },
            bootstrapSampleSize: {
                required: true,
                number: true,
                min: 0
            },
            bootstrapEnsemble: {
                required: true,
                number: true,
                min: 0,
                max: 2
            }
        }, messages: {
            dataset: {
                required: "Please select a dataset."
            }, jvmMaxMem: {
                required: "Must be a number between 0 and 128.",
                min: "Must be at least 0.",
                max: "Must be at most 128."
            },
            alpha: {
                required: "Please set the alpha.",
                min: "Must be at least 0.0.",
                max: "Must be at most 1.0."
            },
            structurePrior: {
                required: "Structure prior is required.",
                min: "Must be at least 1."
            },
            maxDegree: {
                required: "Max degree is required.",
                min: "Must be at least -1."
            },
            maxPathLength: {
                required: "Max path length is required.",
                min: "Must be at least -1."
            },
            bootstrapSampleSize: {
                required: "Sample size is required.",
                min: "Must be at least 0."
            },
            bootstrapEnsemble: {
                required: "Ensemble is required.",
                min: "Must be at least 0.",
                max: "Must be at most 2"
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
