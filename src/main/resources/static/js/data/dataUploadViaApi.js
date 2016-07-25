// Hide message by default
$('#apiMsg').hide();
// Hide the username
$("#apiUsername").hide();

var apiKey = '';
$("#apiKey").on('change keyup paste', function() {
    apiKey = $(this).val();
});

var queryJson = '';
$("#queryJson").on('change keyup paste', function() {
    queryJson = $(this).val();
});

var username = $("#apiUsername").text();

// Separate the ajax request with callbacks
var apiBtn = $('#apiBtn');
apiBtn.click(function() {
    var jqxhr = $.ajax({
        url: "http://localhost:3000/" + username + '/' + apiKey,
        method: 'POST', 
        async : true,
        contentType: "application/json",
        data: queryJson,
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

       