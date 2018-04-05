$('#feedback').on("input", function () {
    if ($('#feedback').val().length > 0) {
        $('#submitFeedback').removeAttr('disabled');
    } else {
        $('#submitFeedback').prop('disabled', true);
    }
});
