$(document).ready(function () {
    var navList = $('div.setup-panel div button');
    var wizard = $('.setup-content');
    var nextBtn = $('.nextBtn');

    wizard.hide();

    navList.click(function (e) {
        e.preventDefault();
        var str = $(this).attr('onclick').replace("window.location.href = '", "").replace("'", "");
        var $target = $(str);
        var $item = $(this);

        if (!$item.hasClass('disabled')) {
            navList.removeClass('btn-primary').addClass('btn-default');
            $item.addClass('btn-primary');
            wizard.hide();
            $target.show();
            $target.find('input:eq(0)').focus();
        }
    });

    nextBtn.click(function () {
        var curStep = $(this).closest(".setup-content");
        var curStepBtn = curStep.attr("id") + 'btn';
        var nextStepWizard = $('div.setup-panel div button[id="' + curStepBtn + '"]').parent().next().children("button");
        var curInputs = curStep.find("input[type='text'],input[type='url']");
        var isValid = true;

        $(".form-group").removeClass("has-error");
        for (var i = 0; i < curInputs.length; i++) {
            if (!curInputs[i].validity.valid) {
                isValid = false;
                $(curInputs[i]).closest(".form-group").addClass("has-error");
            }
        }

        var validator = $("#agorithmJobForm").validate();
        isValid = validator.form();

        if (isValid) {
            nextStepWizard.removeAttr('disabled').trigger('click');
        }
    });

    $('div.setup-panel div button.btn-primary').trigger('click');
});
