package com.axway.apigwgcm.oauth;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.api.CertValidationException;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URLEncoder;
import java.security.cert.CertPath;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 11/19/2014.
 */
public class OAuthClient implements OAuthOperations {

    private static final String TAG = OAuthClient.class.getSimpleName();

    public static final String DEFAULT_GRANT_TYPE = "client_credentials";
    public static final String DEFAULT_SCOPE = "axway.gcm";

    public static final String BASE_URI = "/api/oauth";
    public static final String GET_TOKEN_PATH = BASE_URI + "/token";
    public static final String REVOKE_TOKEN_PATH = BASE_URI + "/revoke";
    public static final String TOKEN_INFO_PATH = BASE_URI + "/tokeninfo";

//    private WeakReference<Context> ctxRef;
    private String clientId;
    private String clientSecret;
    private AccountUtil acctUtil = AccountUtil.getInstance();
    private BaseApp baseApp = BaseApp.getInstance();
//    private static OAuthClient instance = null;

/*
    public static OAuthClient getInstance() {     // final Context ctx) {  //, final String clientId, final String clientSecret, final String host, final int port) {
        if (instance == null)
            instance = new OAuthClient();    //, clientId, clientSecret, host, port);
        return instance;
    }

    protected OAuthClient() {  //final Context ctx, final String clientId, final String clientSecret, final String host, final int port) {
        super();
        this.clientId = Constants.API_GATEWAY_CLIENT_ID;
        this.clientSecret = Constants.API_GATEWAY_CLIENT_SECRET;
    }
*/

    public OAuthClient(String id, String secret) {
        super();
        this.clientId = id;
        this.clientSecret = secret;
    }

    private String encode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return null;
    }

    private String buildOAuthUrl(String[] parts, String endpoint) {
        StringBuilder sb = new StringBuilder(Constants.HTTPS_SCHEME);
        sb.append("://").append(parts[AccountUtil.NDX_HOST]).append(":").append(parts[AccountUtil.NDX_PORT]);
        if (!endpoint.startsWith("/"))
            sb.append("/");
        sb.append(endpoint);
        return sb.toString();
    }

    private Request accessTokenRequest(final String user, final String pwd) {
        if (TextUtils.isEmpty(user))
            return null;
        String[] parts = acctUtil.splitAccountName(user);
        if (parts == null || parts.length != 3) {
            return null;
        }
        String url = buildOAuthUrl(parts, GET_TOKEN_PATH); //new StringBuilder(URI_SCHEME);
        Request.Builder bldr = new Request.Builder();
        bldr.url(url);
        bldr.header("Authorization", Credentials.basic(user, pwd));
        StringBuilder body = new StringBuilder();
        body.append("client_id=").append(encode(clientId))
                .append("&client_secret=").append(encode(clientSecret))
                .append("&grant_type=").append(DEFAULT_GRANT_TYPE)
                .append("&username=").append(parts[AccountUtil.NDX_USERNAME])
                .append("&password=").append(pwd)
                .append("&scope=").append(DEFAULT_SCOPE);
        bldr.method("POST", RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body.toString()));
        return bldr.build();
    }

    @Override
    public OAuthToken getAccessToken(String user, String pwd) throws CertValidationException {
        Request req = accessTokenRequest(user, pwd);
        JsonObject j = null;
        try {
            Response resp = baseApp.executeRequest(req);
            String jstr = baseApp.consumeStringResponse(resp);
            j = JsonUtil.getInstance().parseAsJsonObject(jstr);
//            if (resp.isSuccessful()) {
//                final String jstr = resp.body().string();
//            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
//        finally {
//            if (resp != null) {
//                resp.body().close();
//            }
//        }
        if (j == null)
            return null;
        return OAuthToken.from(j);
    }

    @Override
    public void getAccessToken(String user, String pwd, Callback cb) throws CertValidationException {
        Request req = accessTokenRequest(user, pwd);
        baseApp.executeRequestAsync(req, cb);
    }

    @Override
    public void revokeAccessToken(String user, OAuthToken token, Callback cb) {
        if (TextUtils.isEmpty(user) || token == null)
            return;
        String[] parts = acctUtil.splitAccountName(user);
        if (parts == null || parts.length != 3) {
            return;
        }
        String url = buildOAuthUrl(parts, REVOKE_TOKEN_PATH);
        StringBuilder body = new StringBuilder();
        body.append("client_id=").append(encode(clientId))
            .append("&client_secret=").append(encode(clientSecret))
            .append("&token=").append(encode(token.getAccessToken()));
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .method("POST", RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body.toString()))
                .build();
        baseApp.executeRequestAsync(req, cb);
    }

    @Override
    public void getTokenInfo(String user, OAuthToken token, Callback cb) {
        if (TextUtils.isEmpty(user) || token == null)
            return;
        String[] parts = acctUtil.splitAccountName(user);
        if (parts == null || parts.length != 3) {
            return;
        }
        String url = buildOAuthUrl(parts, TOKEN_INFO_PATH);
        url = url + "?access_token=" + token.getAccessToken();
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        baseApp.executeRequestAsync(req, cb);
    }
}
