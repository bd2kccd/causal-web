var lock = new Auth0Lock(clientId, domain, {
    rememberLastLogin: false,
    auth: {
        redirectUrl: callback,
        responseType: 'code',
        params: {
            state: state,
            scope: 'openid user_id name nickname email picture'
        }
    },
    theme: {
        logo: icon
    },
    languageDictionary: {
        title: 'Causal Web',
        emailInputPlaceholder: 'Pitt or Harvard email.'
    }
});
function signIn() {
    lock.show();
}
