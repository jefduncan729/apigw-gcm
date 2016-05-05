package com.axway.apigwgcm.oauth;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.SafeAsyncTask;

import java.io.IOException;

/**
* Created by su on 12/29/2014.
*/
public class GetTokenTask extends SafeAsyncTask<Void, Void, OAuthToken> {

    private static final String TAG = GetTokenTask.class.getSimpleName();

    public interface Callbacks {
        public void onSuccess(final OAuthToken token);
        public void onCanceled();
    }

    private Callbacks callbacks;
    private AccountUtil acctUtil;

    protected GetTokenTask(Context ctx) {
        super(ctx);
        if (!(ctx instanceof Activity))
            throw new IllegalArgumentException("context passed to " + TAG + " must be an instance of Activity");
        acctUtil = AccountUtil.getInstance(ctx);
        callbacks = null;
    }

    public GetTokenTask(Context ctx, Callbacks callbacks) {
        this(ctx);
        this.callbacks = callbacks;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    protected OAuthToken run(Void... params) {
        int retries = 0;
        OAuthToken rv = null;
        while (rv == null && retries < 3) {
            Log.d(TAG, "requesting AuthToken, attempt " + Integer.toString(retries + 1));
            String t = null;
            try {
                t = acctUtil.getAccountManager().blockingGetAuthToken(acctUtil.getSingleAccount(), Constants.AUTH_TOKEN_TYPE, true);
//                final AccountManagerFuture<Bundle> future = acctUtil.getAuthToken((Activity) getContext());

//                final Bundle bnd = future.getResult();
//                t = bnd.getString(AccountManager.KEY_AUTHTOKEN);
            }
            catch (OperationCanceledException e) {
                Log.d(TAG, "OperationCancelled");
            }
            catch (AuthenticatorException e) {
                Log.d(TAG, "AuthenticatorException");
            }
            catch (IOException e) {
                Log.d(TAG, "IOException");
            }
            final OAuthToken token = OAuthToken.from(t);
            if (token == null || token.isExpired()) {
                ++retries;
                snooze(retries * (DateUtils.SECOND_IN_MILLIS * 5));
            }
            else {
                rv = token;
            }
        }
        return rv;
    }

    @Override
    protected void doPostExecute(OAuthToken oAuthToken) {
        if (callbacks != null)
            callbacks.onSuccess(oAuthToken);
    }

    @Override
    protected void doOnCancelled(OAuthToken oAuthToken) {
        if (callbacks != null)
            callbacks.onCanceled();
    }
}
