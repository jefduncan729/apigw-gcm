package com.axway.apigwgcm.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.api.ActivityCallback;
import com.axway.apigwgcm.events.ActionEvent;
import com.axway.apigwgcm.oauth.GetTokenTask;
import com.axway.apigwgcm.oauth.OAuthClient;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.DateUtil;
import com.axway.apigwgcm.util.GcmUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.google.gson.JsonObject;
import com.squareup.otto.Subscribe;

/**
 * Created by su on 12/11/2014.
 */
abstract public class OAuthBaseActivity extends BaseActivity implements GetTokenTask.Callbacks {

    private static final String TAG = OAuthBaseActivity.class.getSimpleName();

    public static final int MSG_TOKEN_OBTAINED = 5002;

    private GetTokenTask tokenTask;
    private OAuthClient oAuthClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected AccountUtil getAccountUtil() {
        return AccountUtil.getInstance(this);
    }

    protected AccountManager getAccountMgr() {
        return getAccountUtil().getAccountManager();
    }

    protected Account getAccount() {
        return getAccountUtil().getSingleAccount();
    }

    protected String makeBaseUrl() {
        Bundle gcmCfg = GcmUtil.loadSettings(getPrefs());
        StringBuilder sb = new StringBuilder();
        if (gcmCfg.getBoolean(Constants.KEY_SERVICES_USE_SSL, false))
            sb.append(Constants.HTTPS_SCHEME);
        else
            sb.append(Constants.HTTP_SCHEME);
        sb.append("://");
        String[] parts = getAccountUtil().splitAccountName(getAccount());
        if (parts != null && parts.length == 3)
            sb.append(parts[AccountUtil.NDX_HOST]);
        sb.append(":").append(gcmCfg.getInt(Constants.KEY_SERVICES_PORT, 8080));
        return sb.toString();
    }

    protected void confirmInvalidateToken(final boolean forceLogin) {
        Account acct = getAccount();
        if (acct == null)
            return;
        String msg = getString(R.string.confirm_msg, "invalidate access token for " + acct.name);
        confirmDialog(getString(R.string.confirm), msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                invalidateAuthToken(forceLogin);
            }
        }, Constants.NOOP_LISTENER);
    }

    protected void invalidateAuthToken(final boolean forceLogin) {
        String t = getAccountUtil().peekAuthToken();
        OAuthToken oauthToken = OAuthToken.from(t);
        if (oauthToken != null) {
            getAccountUtil().invalidateToken(oauthToken.toString());
        }
        if (forceLogin) {
            Log.d(TAG, "clearing account password");
            getAccountUtil().clearPassword();
        }
        showToast("Access token invalidated");
    }

    protected OAuthToken getAuthToken() {
        final String t = getAccountUtil().peekAuthToken();
        OAuthToken rv = OAuthToken.from(t);
        if (rv == null)
            return null;
        if (rv.isExpired()) {
            getAccountUtil().invalidateToken(rv.toString());
            Log.d(TAG, "AuthToken expired, invalidating token");
            return null;
        }
        Log.d(TAG, "getAuthToken returning " + rv.toString());
        return rv;
    }

    protected void refreshToken() {
        if (tokenTask != null) {
            Log.d(TAG, "tokenTask is non-null, cannot continue");
            return;
        }
        showProgressBar(true);
        Log.d(TAG, "starting getToken task");
        tokenTask = new GetTokenTask(this, this);
        tokenTask.execute();
    }

    @Override
    public void onSuccess(OAuthToken token) {
        tokenTask = null;
        showProgressBar(false);
        Message msg = getMsgHandler().obtainMessage(MSG_TOKEN_OBTAINED);
        msg.obj = token;
        getMsgHandler().sendMessage(msg);
    }

    @Override
    public void onCanceled() {
        showProgressBar(false);
        Log.d(TAG, "getToken task canceled");
        getTaskCanceled();
    }

    protected void getTaskCanceled() {
        tokenTask = null;
        showToast("Operation canceled");
        finish();
    }

    protected void onTokenInfo(String s) {
        showProgressBar(false);
        JsonObject json = JsonUtil.getInstance().parseAsJsonObject(s);
        if (json == null) {
            showToast("invalid response from server");
            return;
        }
        String u = json.get("user_id").getAsString();
        int e = json.get("expires_in").getAsInt();
        String units = "minute";
        if (e < DateUtil.SECONDS_PER_MINUTE) {
            units = "second";
        }
        else {
            e = (e / DateUtil.SECONDS_PER_MINUTE);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Token for ").append(u).append(" expires in about ");
        sb.append(e).append(" ").append(units).append((e == 1 ? "" : "s"));
        showToast(sb.toString());
    }

    protected void confirmRevokeToken() {
        String s = getAccountMgr().peekAuthToken(getAccount(), Constants.AUTH_TOKEN_TYPE);
        if (TextUtils.isEmpty(s))
            return;
        final OAuthToken t = OAuthToken.from(s);
        if (t == null)
            return;
        String msg = getString(R.string.confirm_msg, "revoke access token for " + getAccount().name);
        confirmDialog(msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                revokeToken(t);
            }
        });
    }

    protected void revokeToken(final OAuthToken t) {
        getOAuthClient().revokeAccessToken(getAccount().name, t, new RevokeCallback(this));
    }

    protected void onTokenRevoked(boolean revoked) {
        showProgressBar(false);
        Log.d(TAG, "onTokenRevoked: " + Boolean.toString(revoked));
        getAccountUtil().clearPassword();
        showToast(R.string.auth_revoked);
        finish();
    }

    protected void getTokenInfo() {
        String s = getAccountUtil().peekAuthToken();
        final OAuthToken t = OAuthToken.from(s);
        final String user = getAccountUtil().getSingleAccount().name;
        if (t == null || t.isExpired()) {
            notifyInvalidToken(t);
            return;
        }
        showProgressBar(true);
        getOAuthClient().getTokenInfo(user, t, new InfoCallback(this));
    }

    protected void notifyInvalidToken(final OAuthToken token) {
        Log.d(TAG, "invalid token");
        String msg = getString(R.string.confirm_msg, "refresh access token");
        String title = (token == null ? "Invalid Token" : "Access Token Expired");
        alertDialog(title, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (token == null) {
                    Log.d(TAG, "refreshing token");
                    refreshToken();
                }
                else {
                    Log.d(TAG, "invalidating token");
                    invalidateAuthToken(false);
                    refreshToken();
                }
            }
        }, Constants.NOOP_LISTENER);
    }

    protected OAuthClient getOAuthClient() {
        if (oAuthClient == null) {
            oAuthClient = new OAuthClient(Constants.API_GATEWAY_CLIENT_ID, Constants.API_GATEWAY_CLIENT_SECRET);
        }
        return oAuthClient;
    }

    private class InfoCallback extends ActivityCallback {

        public InfoCallback(Activity activity) {
            super(activity);
        }

        @Override
        protected void onSuccessResponse(int code, String msg, final String body) {
            onTokenInfo(body);
        }
    }

    private class RevokeCallback extends ActivityCallback {

        public RevokeCallback(Activity activity) {
            super(activity);
        }

        @Override
        protected void onSuccessResponse(int code, String msg, String body) {
            onTokenRevoked(true);
        }
    }
}
