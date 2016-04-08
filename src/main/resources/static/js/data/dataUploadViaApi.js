// Hide message by default
$('#apiMsg').hide();
// Hide the username
$("#apiUsername").hide();

var whereFields = '';
$("#whereFields").on('change keyup paste', function() {
    whereFields = $(this).val();
});

var selectFields = '';
$("#selectFields").on('change keyup paste', function() {
    selectFields = $(this).val();
});

var username = $("#apiUsername").text();

// Separate the ajax request with callbacks
var apiBtn = $('#apiBtn');
apiBtn.click(function() {
    var jqxhr = $.ajax({
        url: "http://localhost:3000/" + username + '/' + encodeURIComponent(whereFields) + '/' + encodeURIComponent(selectFields),
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

       