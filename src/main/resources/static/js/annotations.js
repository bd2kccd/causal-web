const apiURL = "http://localhost:8000/api";
const tokenURL = apiURL+"/oauth/token";

function getAnnotationsToken() {
    const client = "causal-web";
    const clientPassword = "";
    const grantType = "password";
    const username = document.getElementById("login").username.value;
    const password = document.getElementById("login").password.value;

    $.ajax({
        url: tokenURL,
        type: 'post',
        data: {
            grant_type: grantType,
            username: username,
            password: password
        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa(client + ':' + clientPassword));
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        },
        dataType: 'json',
        success: function(data) {
            var expires = new Date();
            expires.setTime(expires.getTime() + (data['expires_in']*1000));
            document.cookie = "access_token="+data['access_token']+"; expires=" + expires.toGMTString();
            sessionStorage.setItem('refresh_token', data['refresh_token']);
        },
        error: function(data) {
            console.log("Error fetching annotations access token\ndata: " + data);
        }
    });
}