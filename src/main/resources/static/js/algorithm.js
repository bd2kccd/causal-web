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

$(document).ready(function () {
    $("#continuous").prop("disabled", true);
    if($('.dataset-multiple')){
    	$('.dataset-multiple').multiselect({
    		includeSelectAllOption: true,
            enableFiltering: true,
            onChange: function(option, checked){
            	var selectedDatasets = '';
            	$('.dataset-multiple option:selected').each(function(){
            		if(selectedDatasets != ''){
            			selectedDatasets += ',';
            		}
            		selectedDatasets += $(this).val();
            		$('#dataset-summary').text(selectedDatasets);
            		$('#dataset').val(selectedDatasets);
            	});
            }
        });
    }
});
