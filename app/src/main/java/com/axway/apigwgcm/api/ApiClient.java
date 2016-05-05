package com.axway.apigwgcm.api;

import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.util.StringUtil;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 1/25/2016.
 */
public class ApiClient {

    private static final String TAG = ApiClient.class.getSimpleName();

//    private ServerInfo srvr;
    private static OkHttpClient httpClient;
    private boolean forceTrailingSlash;
    private String uname;
    private String passwd;
    private boolean isSsl;

    protected ApiClient() {
        super();
        httpClient = null;
//        srvr = null;
        isSsl = true;
        uname = null;
        passwd = null;
        forceTrailingSlash = false;
    }

//    protected ApiClient(ServerInfo srvr) {
//        this();
//        this.srvr = srvr;
//    }
//
//    public static ApiClient from(ServerInfo srvr) {
//        ApiClient rv = new ApiClient(srvr);
//        return rv;
//    }

    public ApiClient(String uname, String passwd) {
        this();
        this.uname = uname;
        this.passwd = passwd;
    }

    public ApiClient(String uname, String passwd, boolean isSsl) {
        this(uname, passwd);
        this.isSsl = isSsl;
    }

    public static OkHttpClient getHttpClient() {
//        assert srvr != null;
        if (httpClient == null) {
            OkHttpClient.Builder bldr = new OkHttpClient.Builder()
                    .readTimeout(300, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .hostnameVerifier(BaseApp.hostVerifier());
            bldr.sslSocketFactory(BaseApp.sslSocketFactory());
//            this.uname = uname;
//            this.passwd = passwd;
            httpClient = bldr.build();
        }
        return httpClient;
    }

    protected String buildUrl(String endpoint) {
//        assert srvr != null;
        StringBuilder sb = new StringBuilder();
//        if (srvr != null) {
//            sb.append(srvr.isSsl() ? Constants.HTTPS_SCHEME : Constants.HTTP_SCHEME).append("://");
//            sb.append(srvr.getHost()).append(":").append(srvr.getPort());
//        }
        if (!endpoint.startsWith("/"))
            sb.append("/");
        sb.append(endpoint);
        String rv = sb.toString();
        if (forceTrailingSlash && !rv.endsWith("/"))
            rv += "/";
        return rv;
    }

    public boolean isForceTrailingSlash() {
        return forceTrailingSlash;
    }

    public void setForceTrailingSlash(boolean forceTrailingSlash) {
        this.forceTrailingSlash = forceTrailingSlash;
    }

//    public void get(String endpoint, ResponseHandler<JsonObject> handler) {
//        sendRequest(endpoint, handler);
//        String cred = Credentials.basic(srvr.getUser(), srvr.getPasswd());
//        Request req = new Request.Builder()
//                .url(buildUrl(endpoint))
//                .header("Authorization", cred)
//                .build();
//        GetTask<JsonObject> t = new GetTopologyTask(handler);
//        t.execute(req);
//    }

    public Request createRequest(final String endpoint) {
        final Request.Builder bldr = new Request.Builder();
        bldr.url(buildUrl(endpoint));
        if (!TextUtils.isEmpty(uname)) {
            bldr.header("Authorization", Credentials.basic(uname, passwd));
        }
        return bldr.build();
    }

    public void reset() {
        BaseApp.resetSocketFactory();
        httpClient = null;
    }

    public Request createRequest(final String endpoint, JsonObject obj) {
        return createRequest(endpoint, "POST", obj);
    }

    public Request createRequest(final String endpoint, String method, JsonObject obj) {
//        String cred = Credentials.basic(srvr.getUser(), srvr.getPasswd());
        final Request.Builder bldr = new Request.Builder();
        bldr.url(buildUrl(endpoint))
            .method(method, RequestBody.create(MediaType.parse("application/json"), (obj == null ? "" : obj.toString())));
        if (!TextUtils.isEmpty(uname)) {
            bldr.header("Authorization", Credentials.basic(uname, passwd));
        }
        return bldr.build();
    }

    public Response executeRequest(final Request req) throws IOException {
        Log.d(TAG, StringUtil.format("executeRequest: %s", req));
        assert httpClient != null;
        Response resp = httpClient.newCall(req).execute();
        Log.d(TAG, StringUtil.format("response: %s", resp));
        return resp;
    }

    public void executeAsyncRequest(final Request req, final Callback callback) {
        assert httpClient != null;
        Log.d(TAG, StringUtil.format("executeRequestAsync: %s", req));
        httpClient.newCall(req).enqueue(callback);
    }

    public void checkCert(Callback callback) {
//        if (srvr == null)
//            return;
        executeAsyncRequest(createRequest("api"), callback);
    }

//    public ServerInfo getServerInfo() {
//        return srvr;
//    }
//    protected void sendRequest(final String endpoint, final ResponseHandler handler) {
//        String cred = Credentials.basic(srvr.getUser(), srvr.getPasswd());
//        final Request req = new Request.Builder()
//                .url(buildUrl(endpoint))
//                .header("Authorization", cred)
//                .build();
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Log.d(TAG, StringUtil.format("execute request: %s", req.toString()));
//                    Response resp = getHttpClient().newCall(req).execute();
//                    Log.d(TAG, StringUtil.format("data: %s", resp.toString()));
//                    if (resp.isSuccessful()) {
//                        if (handler != null) {
//                            JsonObject obj = JsonHelper.getInstance().parseAsObject(resp.body().string());
//                            if (obj != null && obj.has("result")) {
//                                obj = obj.getAsJsonObject("result");
//                            }
//                            handler.onSuccess(resp.code(), resp.headers(), obj);
//                        }
//                    }
//                }
//                catch (IOException e) {
//                    if (handler != null) handler.onFailure(-1, e);
//                }
//            }
//        }.start();
//    }
//
//    private final class GetTopologyTask extends GetTask<JsonObject> {
//
//        public GetTopologyTask(ResponseHandler<JsonObject> handler) {
//            super(handler, "result");
//        }
//
//        @Override
//        protected JsonObject parseResponse(Response resp) {
//            JsonObject rv = null;
//            try {
//                String s = resp.body().string();
//                rv = JsonHelper.getInstance().parseAsObject(s);
//                if (rv != null && !TextUtils.isEmpty(attrName)) {
//                    rv = rv.getAsJsonObject(attrName);
//                }
//            }
//            catch (Exception e) {
//                if (handler != null)
//                    handler.onFailure(code, e);
//            }
//            return rv;
//        }
//
//        @Override
//        protected OkHttpClient getClient() {
//            return getHttpClient();
//        }
//    }
}
