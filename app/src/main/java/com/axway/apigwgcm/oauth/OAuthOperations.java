package com.axway.apigwgcm.oauth;

import com.axway.apigwgcm.api.CertValidationException;

import okhttp3.Callback;

/**
 * Created by su on 11/19/2014.
 */
public interface OAuthOperations {

    public OAuthToken getAccessToken(final String user, final String pwd) throws CertValidationException;

    public void getAccessToken(final String user, final String pwd, final Callback cb) throws CertValidationException;
    public void revokeAccessToken(final String user, final OAuthToken token, final Callback cb);
    public void getTokenInfo(final String user, final OAuthToken token, final Callback cb);
}
