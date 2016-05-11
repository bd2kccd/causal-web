var apiURL = "http://localhost:8000/api";
var tokenURL = apiURL+"/oauth/token";

function getAnnotationsToken() {
    var client = "curl";
    var clientPassword = "";
    var username = document.getElementById("login").username.value;
    var password = document.getElementById("login").password.value;

    console.log("fetching annotations token for " + "username: " + username);

    var params = "grant_type=password&username="+username+"&password="+password;

    console.log("groups test");
    $.ajax({
        url: apiURL+"/groups",
        type: 'get',
        headers: {
            'Authorization': 'Bearer 373aca45-3bad-4c2d-af86-ed84b384ff26',
        },
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization','Bearer 373aca45-3bad-4c2d-af86-ed84b384ff26');
        },
        success: function(data) {
            console.log("Success\ndata: " + data)
        },
        error: function(data) {
            console.log("Error\ndata: " + data)
        }
    });

    console.log("token test");
    $.ajax({
        url: tokenURL,
        type: 'post',
        data: {
            grant_type: 'password',
            username: username,
            password: password
        },
        headers: {
            'Authorization': 'Basic ' + btoa(client+":"+clientPassword),
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        dataType: 'json',
        success: function(data) {
            console.log(data)
            console.log(data['access_token']);
            console.log("expires in: " + data['expires_in']);
            console.log("expires at: " + new Date(((new Date()).getTime() + data['expires_in']*1000)));
        }
    });
}