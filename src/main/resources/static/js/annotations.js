const apiURL = "http://localhost:8000/api";
const tokenURL = apiURL+"/oauth/token";

function getAnnotationsToken() {
    const client = "curl";
    const clientPassword = "";
    const grantType = "password";
    const username = document.getElementById("login").username.value;
    const password = document.getElementById("login").password.value;

    console.log("fetching annotations token for " + "username: " + username);

    console.log("groups test");
    $.ajax({
        url: apiURL+"/groups",
        type: 'get',
        headers: {
            'Authorization': 'Bearer 8dcd34a8-7af8-457f-a639-757281c9179a',
        },
        dataType: 'json',
        success: function(data) {
            console.log("Success\ndata: " + data)
            console.log("Group: " + data['_embedded']['groups'][0]['name']);
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
            grant_type: grantType,
            username: username,
            password: password
        },
//        headers: {
//            'Authorization': 'Basic ' + btoa(client + ":" + clientPassword),
//            'Content-Type': 'application/x-www-form-urlencoded'
//        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa(client + ':' + clientPassword));
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
//            xhr.setRequestHeader('Accept', 'application/json');
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