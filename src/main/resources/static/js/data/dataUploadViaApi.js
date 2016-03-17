// Hide message by default
$('#apiMsg').hide();


// Separate the ajax request with callbacks
var apiBtn = $('#apiBtn');
apiBtn.click(function() {
    var jqxhr = $.ajax({
        url: "http://localhost:3000/zhy19/demographics%2FRACE%2Fwhite!!demographics%2FSEX%2Fmale",
        method: 'GET', 
        async : true,
        dataType: "text"
    });

    jqxhr.done(function(data) {
        console.log('PIC-SURE API data loaded successfully.');
        $('#apiMsg').show();
    });

    jqxhr.fail(function () { 
        console.log('Ajax error - failed to load data from PIC-SURE API');
    });
});    

       