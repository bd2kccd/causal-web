/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */

const apiURL = "http://localhost:8000/api";
const tokenURL = apiURL+"/oauth/token";
const client = "causal-web";
const clientPassword = "";
const passwordGrant = "password";
const refreshGrant = "refresh_token";

/**
 * Fetch refresh and access tokens from Annotations API
 */
function requestAnnotationsTokens() {
    $.ajax({
        url: tokenURL,
        type: 'post',
        data: {
            grant_type: passwordGrant,
            username: document.getElementById("login").username.value,
            password: document.getElementById("login").password.value
        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa(client + ':' + clientPassword));
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        },
        dataType: 'json',
        success: function(data) {
            storeAccessToken(data['access_token'], data['expires_in']);
            storeRefreshToken(data['refresh_token']);
        }
    });
}

/**
 * Save access token in cookie
 * @param  {string} token access token
 * @param  {int} expires  token expiration in seconds
 */
function storeAccessToken(token, expires) {
    var date = new Date();
    date.setTime(date.getTime() + expires*1000);
    document.cookie = "access_token=" + token + "; expires=" + date.toGMTString();
}

/**
 * Get access token from cookies
 * @return {string} access token
 */
function getAccessToken() {
    var value = "; " + document.cookie;
    var parts = value.split("; access_token=");
    if (parts.length == 2) {
        return parts.pop().split(";").shift();
    } else {
        return requestAccessToken();
    }
}

/**
 * Save refresh token in session storage
 * @param  {string} token refresh token
 */
function storeRefreshToken(token) {
    sessionStorage.setItem('refresh_token', token);
}

/**
 * Get refresh token from session storage
 * @return {string} refresh token
 */
function getRefreshToken() {
    return sessionStorage.getItem('refresh_token');
}

/**
 * Fetch new access token using refresh token
 * @return {string} access token
 */
function requestAccessToken() {
    $.ajax({
        url: tokenURL,
        type: 'post',
        data: {
            grant_type: refreshGrant,
            refresh_token: getRefreshToken()
        },
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa(client + ':' + clientPassword));
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        },
        dataType: 'json',
        success: function(data) {
            var accessToken = data['access_token'];
            var expires = data['expires_in'];
            storeAccessToken(accessToken, expires);
            return accessToken;
        },
        error: function(data) {
            console.log("Error fetching access token");
            return "";
        }
    });
}

function getUser() {
    $.ajax({
        url: apiURL+"/users/1",
        type: 'get',
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
        },
        dataType: 'json',
        success: function(data) {
            console.log("Success fetching user: " + data['username']);
        }
    });
}
