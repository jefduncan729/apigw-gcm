package com.axway.apigwgcm;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.oauth.OAuthClient;
import com.axway.apigwgcm.util.StringUtil;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by su on 3/22/2016.
 */
public class BaseApp extends Application {
    private static final String TAG = BaseApp.class.getSimpleName();

    private static SSLSocketFactory _socketFactory = null;
    private static KeystoreManager _keystoreManager = null;
    private static HostnameVerifier _hostnameVerifier = null;
    private static OkHttpClient _httpClient = null;
    private static BaseApp _inst = null;
    private static Bus _bus = null;
    private static OAuthClient _oauthClient = null;

    @Override
    public void onCreate() {
        super.onCreate();
        _inst = this;
    }

    public static BaseApp getInstance() {
        return _inst;
    }

    public static SSLSocketFactory sslSocketFactory() {
        if (_socketFactory == null) {
            Log.d(TAG, "create SSLSocketFactory");
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keystoreManager().loadKeystore());
                sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
                _socketFactory = sslContext.getSocketFactory();
                Log.d(TAG, "SSLSocketFactory created");
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "NoSuchAlgorithmException", e);
            } catch (KeyStoreException e) {
                Log.e(TAG, "KeyStoreException", e);
            } catch (KeyManagementException e) {
                Log.e(TAG, "KeyManagementException", e);
            }
        }
        return _socketFactory;
    }

    public static KeystoreManager keystoreManager() {
        if (_keystoreManager == null) {
            _keystoreManager = KeystoreManager.from(_inst);
        }
        return _keystoreManager;
    }

    public static HostnameVerifier hostVerifier() {
        if (_hostnameVerifier == null) {
            Log.d(TAG, "building hostVerifier");
            _hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };
        }
        return _hostnameVerifier;
    }

    public static OkHttpClient httpClient() {
        if (_httpClient == null) {
            Log.d(TAG, "building httpClient");
            OkHttpClient.Builder bldr = new OkHttpClient.Builder()
                    .readTimeout(300, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .hostnameVerifier(hostVerifier());
            bldr.sslSocketFactory(sslSocketFactory());
            _httpClient = bldr.build();
        }
        return _httpClient;
    }

    public Response executeRequest(Request req) throws IOException {
        assert req != null;
        Log.d(TAG, StringUtil.format("executeRequest: %s", req));
        Response resp = httpClient().newCall(req).execute();
        Log.d(TAG, StringUtil.format("response: %s", resp));
        return resp;
    }

    public void executeRequestAsync(Request req, Callback cb) {
        assert req != null;
        assert cb != null;
        Log.d(TAG, StringUtil.format("executeRequestAsync: %s", req));
        httpClient().newCall(req).enqueue(cb);
    }

    public String consumeStringResponse(final Response resp) throws IOException {
        if (resp == null)
            return null;
        String rv = null;
        try {
            if (resp.isSuccessful()) {
                rv = resp.body().string();
            }
        }
        finally {
            resp.body().close();
        }
        return rv;
    }

    public static Bus bus() {
        if (_bus == null) {
            _bus = new Bus();
        }
        return _bus;
    }

    public static void post(Object evt) {
        Log.d(TAG, StringUtil.format("postEvent: %s", evt));
        bus().post(evt);
    }

    public static void resetSocketFactory() {
        Log.d(TAG, "resetSocketFactory");
        _socketFactory = null;
    }

    public static void resetHttpClient() {
        Log.d(TAG, "resetHttpClient");
        _httpClient = null;
    }

    public static boolean addTrustedCert(String alias, CertPath cp) {
        boolean rv = keystoreManager().addTrustedCert(alias, cp);
        if (rv) {
            resetSocketFactory();
            resetHttpClient();
        }
        return rv;
    }

    public static CertPath certPathFromThrowable(Throwable e) {
        CertPathValidatorException cpve = null;
        Throwable cause = e.getCause();
        while (cpve == null && cause != null) {
            if (cause instanceof CertPathValidatorException) {
                cpve = (CertPathValidatorException)cause;
            }
            else
                cause = cause.getCause();
        }
        if (cpve != null)
            return cpve.getCertPath();
        return null;
    }

    public void saveOAuthCreds(final String id, final String secret) {
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(secret)) {
            Log.d(TAG, "missing oauth client id/secret");
            return;
        }
        Log.d(TAG, StringUtil.format("save client id: %s, secret: %s", id, secret));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString(Constants.KEY_CLIENT_ID, id)
                .putString(Constants.KEY_CLIENT_SECRET, secret)
                .apply();
    }

    public int getNumRecents() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getInt(Constants.KEY_NUM_RECENTS, Constants.DEF_NUM_RECENTS);
    }

    public void removeOAuthCreds() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .remove(Constants.KEY_CLIENT_ID)
                .remove(Constants.KEY_CLIENT_SECRET)
                .apply();
        Log.d(TAG, "oauth creds removed");
    }

    public OAuthClient oAuthClient() {
        if (_oauthClient == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String id = prefs.getString(Constants.KEY_CLIENT_ID, null);
            String secret = prefs.getString(Constants.KEY_CLIENT_SECRET, null);
            if (TextUtils.isEmpty(id) || TextUtils.isEmpty(secret)) {
                Log.d(TAG, "missing oauth client id/secret");
                return null;
            }
            Log.d(TAG, StringUtil.format("build oAuthClient id: %s, secret: %s", id, secret));
            _oauthClient = new OAuthClient(id, secret);
        }
        return _oauthClient;
    }
}
