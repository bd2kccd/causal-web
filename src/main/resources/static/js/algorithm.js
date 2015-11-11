$(document).on('click', '.panel-heading span.clickable', function (e) {
    var $this = $(this);
    if (!$this.hasClass('panel-collapsed')) {
        $this.parents('.panel').find('.panel-body').slideUp();
        $this.addClass('panel-collapsed');
        $this.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
    } else {
        $this.parents('.panel').find('.panel-body').slideDown();
        $this.removeClass('panel-collapsed');
        $this.find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
    }
});

function sync() {
    var selection = '';
    $('#dataset option:selected').each(function () {
        if (selection !== '') {
            selection += ',';
        }
        selection += $(this).val();
    });
    $('#datasetSelection').text(selection);
}

$(document).ready(function () {
    $("#continuous").prop("disabled", true);
    $('#dataset').multiselect({
        includeSelectAllOption: true,
        enableFiltering: true,
        disableIfEmpty: true,
        buttonWidth: '100%',
        onChange: function (option, checked) {
            $('#datasetSelection').text($('.multiselect-selected-text').text());
        }
    });
    sync();
});
