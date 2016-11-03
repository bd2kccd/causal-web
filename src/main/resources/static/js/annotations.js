/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */

const passwordGrant = "password";
const refreshGrant = "refresh_token";
const tokenURL = "oauth/token";
const annoURL = "annotations/";
const vocabURL = "vocabularies/";
const client = "causal-web";
const clientPassword = "";

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
    params = params.replace(/%2B/g, '+');  // replace %2B with +
    params = params.replace(/%5F/g, '_');  // replace %5F with _
    params = params.replace(/%2C/g, ',');  // replace %2C with ,

    // get async setting
    var a = (typeof async === 'undefined') ? true : async;
    return $.ajax({
        url: annoApiUrl + url + params,
        type: 'get',
        async: a,
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
            typeof beforeSendCallback === 'function' && beforeSendCallback();
        },
        dataType: 'json',
        error: function(xhr) {
            if(xhr.responseText.error_description.startsWith("Access token expired")) {
                console.log('Access token expired. Fetching a new one.')
                requestAccessToken(false);
                return getRequest(url, parameters, false, beforeSendCallback);
            }
        }
    });
}

/**
 * ajax GET request where entire link is supplied
 * @param  {string} link link to request
 * @return  ajax
 */
function getRequestByLink(link) {
    return $.ajax({
        url: link,
        type: 'get',
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
        },
        dataType: 'json',
        error: function(xhr) {
            if (xhr.responseText.error_description.startsWith("Access token expired")) {
                console.log('Access token expired. Fetching a new one.')
                requestAccessToken(false);
                return getRequestByLink(link);
            }
        }
    });
}

/**
 * Generic ajax POST request to CCD Annotations API
 * @param  {string} url request url
 * @param  {object} parameters HTTP body parameters
 * @param  {boolean} async asynchronous request (optional, default true)
 * @param  {closure} beforeSendCallback function performed before sending (optional)
 * @return ajax
 */
function postRequest(url, parameters, async, beforeSendCallback) {
    var a = (typeof async === 'undefined') ? true : async;
    return $.ajax({
        url: annoApiUrl + url,
        type: 'post',
        async: a,
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + getAccessToken());
            xhr.setRequestHeader('Content-Type', 'application/json');
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
function requestAnnotationsTokens(async) {
    var a = (typeof async === 'undefined') ? true : async;
    $.ajax({
        url: annoApiUrl + tokenURL,
        type: 'post',
        async: a,
        data: {
            grant_type: passwordGrant,
            username: document.getElementById("login").loginUsername.value,
            password: document.getElementById("login").loginPassword.value
        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa(annoClientId + ':' + annoClientSecret));
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
        url: annoApiUrl + tokenURL,
        type: 'post',
        async: a,
        data: {
            grant_type: refreshGrant,
            refresh_token: getRefreshToken()
        },
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa(annoClientId + ':' + annoClientSecret));
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
    // delete old cookie
    document.cookie = 'access_token=; Path=/ccd; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    // save new cookie
    document.cookie = 'access_token=' + token + "; Path=/ccd; expires=" + date + "; max_age=" + date;
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
//        url: annoApiUrl+"/users/12",
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
