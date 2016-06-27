/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */

const apiURL = "http://localhost:8000/api";
const tokenURL = "/oauth/token";
const client = "causal-web";
const clientPassword = "";
const passwordGrant = "password";
const refreshGrant = "refresh_token";

/**
 * Generic ajax GET request to CCD Annotations API
 * @param  {string} url request url
 * @param  {object} parameters HTTP parameters
 * @param  {boolean} async asynchronous request (default true)
 * @param  {closure} beforeSendCallback function performed before sending
 * @return  ajax
 */
function getRequest(url, parameters, async, beforeSendCallback) {
    // get HTTP parameters
    var params = (typeof parameters === 'undefined') ? '' : '?' + $.param(parameters);
    // get async setting
    var a = (typeof async === 'undefined') ? true : async;
    return $.ajax({
        url: apiURL + url + params,
        type: 'get',
        async: a,
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
            typeof beforeSendCallback === 'function' && beforeSendCallback();
        },
        dataType: 'json',
        error: function(xhr) {
            if(xhr.responseText.error_description.startsWith("Access token expired")) {
                requestAccessToken(false);
                return getRequest(url, parameters, false, beforeSendCallback);
            }
        }
    });
}

/**
 * Generic ajax POST request to CCD Annotations API
 * @param  {string} url request url
 * @param  {object} parameters HTTP body parameters
 * @param  {boolean} async asynchronous request (default true)
 * @param  {closure} beforeSendCallback function performed before sending
 * @return ajax
 */
function postRequest(url, parameters, async, beforeSendCallback) {
    var a = (typeof async === 'undefined') ? true : async;
    return $.ajax({
        url: apiURL + url,
        type: 'post',
        async: a,
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
            typeof beforeSendCallback === 'function' && beforeSendCallback();
        },
        data: JSON.stringify(parameters),
        contentType: 'application/json',
        dataType: 'json',
        error: function(xhr) {
            if(xhr.responseText.error_description.startsWith("Access token expired")) {
                requestAccessToken(false);
                return postRequest(url, parameters, false, beforeSendCallback);
            }
        }
    });
}

/**
 * Fetch refresh and access tokens from Annotations API
 */
function requestAnnotationsTokens() {
    $.ajax({
        url: apiURL + tokenURL,
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
 * Fetch new access token using refresh token
 * @return {string} access token
 */
function requestAccessToken(async) {
    var a = (typeof async === 'undefined') ? true : async;
    $.ajax({
        url: apiURL + tokenURL,
        type: 'post',
        async: a,
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
            return "";
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
    //  set expiration date to current time + expiration
    date.setTime(date.getTime() + expires*1000);
    date = date.toGMTString();
    document.cookie = "access_token=" + token + "; expires=" + date + "; max_age=" + date;
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

function getUser() {
    getRequest('/users/12');
//    $.ajax({
//        url: apiURL+"/users/12",
//        type: 'get',
//        beforeSend: function (xhr) {
//            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
//        },
//        dataType: 'json',
//        success: function(data) {
//            console.log("Success fetching user: " + data['username']);
//        },
//        error: function(xhr, textStatus, errorThrown) {
//            console.log('text: ' + textStatus + '\n error: ' + errorThrown);
//            console.log(xhr.responseText);
//        }
//    });
}
