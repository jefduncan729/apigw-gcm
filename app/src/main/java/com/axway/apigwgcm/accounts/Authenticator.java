package com.axway.apigwgcm.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.oauth.OAuthClient;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.util.AccountUtil;

/**
 * Created by su on 11/15/2014.
 */
public class Authenticator extends AbstractAccountAuthenticator {

    private static final String TAG = Authenticator.class.getSimpleName();

    private Context context;
    private AccountUtil acctUtil;

    public Authenticator(Context ctx) {
        super(ctx);
        context = ctx;
        acctUtil = AccountUtil.getInstance(ctx);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String acctType, String authTokenType, String[] reqFeat, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "addAccount");
        if (options != null && !options.isEmpty()) {
            Log.d(TAG, "options: " + options.toString());
        }
        final Bundle rv = new Bundle();
        //if there is one and only one account, get it; if not, we'll add the account
        final Account acct = acctUtil.getSingleAccount();
        Log.d(TAG, "starting AuthActivity");
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        rv.putParcelable(AccountManager.KEY_INTENT, intent);
        intent.setAction(Intent.ACTION_INSERT);
        if (acct != null) {
            intent.putExtra(Constants.EXTRA_ITEM_NAME, acct.name);
        }
        return rv;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        Log.d(TAG, "confirmCredentials");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "getAuthToken");
        if (options != null && !options.isEmpty()) {
            Log.d(TAG, "options: " + options.toString());
        }
        final Bundle rv = new Bundle();
        String token = null;
        final String pwd = acctUtil.getAccountManager().getPassword(account);
        if (!TextUtils.isEmpty(pwd)) {
            try {
                OAuthClient client = BaseApp.getInstance().oAuthClient();
                OAuthToken oAuthToken = client.getAccessToken(account.name, pwd);
                if (oAuthToken != null && !TextUtils.isEmpty(oAuthToken.getAccessToken())) {
                    token = oAuthToken.toString();
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage(), e);
            }
        }

        if (!TextUtils.isEmpty(token)) {
            rv.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            rv.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
            rv.putString(AccountManager.KEY_AUTHTOKEN, token);
            return rv;
        }

        //if we're here we need to re-prompt for credentials
        Log.d(TAG, "starting AuthActivity");
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(Constants.EXTRA_ITEM_NAME, account.name);
        intent.putExtra(Constants.EXTRA_ITEM_TYPE, authTokenType);
        intent.setAction(Intent.ACTION_DEFAULT);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        rv.putParcelable(AccountManager.KEY_INTENT, intent);
        return rv;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(TAG, "getAuthTokenLabel: " + authTokenType);
        return authTokenType;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "updateCredentials");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle rv = new Bundle();
        Log.d(TAG, "hasFeatures");
        rv.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return rv;
    }
}
