package com.axway.apigwgcm.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.oauth.OAuthToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by su on 11/24/2014.
 */
public class AccountUtil {

    private static final String TAG = AccountUtil.class.getSimpleName();

    public static final int NDX_USERNAME = 0;
    public static final int NDX_HOST = 1;
    public static final int NDX_PORT = 2;

    private AccountManager acctMgr;
    private Account curAcct;

    private static AccountUtil instance = null;

    protected AccountUtil(Context ctx) {
        super();
        curAcct = null;
        acctMgr = AccountManager.get(ctx);
    }

    public static AccountUtil getInstance(Context ctx) {
        if (instance == null) {
            instance = new AccountUtil(ctx);
        }
        return instance;
    }

    public static AccountUtil getInstance() {
        if (instance == null)
            throw new IllegalStateException("AccountUtil has not been initialized. Probably a programming error.");
        return instance;
    }

    public String[] splitAccountName(Account acct) {
        if (acct == null)
            return null;
        return splitAccountName(acct.name);
    }

    /**
     * Splits the account name into its requisite parts: username, host and port. Must be in the format [user]@[host]:[port]. If not, the return value will be null.
     *
     * @param name the name of the account
     * @return a String array with three elements, or null
     */
    public String[] splitAccountName(String name) {
        if (TextUtils.isEmpty(name))
            return null;
        String[] tmp1 = name.split("@");
        String[] tmp2 = null;
        if (tmp1.length == 2) {
            tmp2 = tmp1[1].split(":");
            if (tmp2.length != 2)
                return null;
        }
        if (tmp2 == null)
            return null;
        String[] rv = new String[3];
        rv[NDX_USERNAME] = tmp1[0];
        rv[NDX_HOST] = tmp2[0];
        rv[NDX_PORT] = tmp2[1];
        return rv;
    }

    public String buildAccountName(String user, String host, int port) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(host))
            return null;
        return StringUtil.format("%s@%s:%d", user, host, port);
    }

    public AccountManager getAccountManager() {
        return acctMgr;
    }

    public Account[] getAccounts() {
        if (acctMgr == null)
            return null;
        return getAccountManager().getAccountsByType(Constants.ACCOUNT_TYPE);
    }

    public boolean isValidAccount(Account acct) {
        if (acct == null)
            return false;
        return isValidAccount(acct.name);
    }

    public boolean isValidAccount(String name) {
        if (acctMgr == null || TextUtils.isEmpty(name))
            return false;
        Account[] accts = getAccounts();
        boolean rv = false;
        for (int i = 0; !rv && i < accts.length; i++) {
            rv = (accts[i].name.equals(name));
        }
        return rv;
    }

    public ArrayList<String> getAccountNames() {
        Account[] accts = getAccounts();
        if (accts == null || accts.length == 0)
            return null;
        ArrayList<String> rv = new ArrayList<String>();
        for (int i = 0; i < accts.length; i++) {
            rv.add(accts[i].name);
        }
        return rv;
    }

    public Account getAccount(String name) {
        Account[] accts = getAccounts();
        if (accts == null || accts.length == 0)
            return null;
        Account rv = null;
        for (int i = 0; (rv == null) && (i < accts.length); i++) {
            if (name.equals(accts[i].name))
                rv = accts[i];
        }
        return rv;
    }

    public void setCurrentAccount(Account newVal) {
        curAcct = newVal;
    }

    public Account getSingleAccount() {
        if (curAcct == null) {
            Account[] accts = getAccounts();
            if (accts != null && accts.length > 0) {
                curAcct = accts[0];
            }
        }
        return curAcct;
    }

    public void invalidateToken(final String t) {
        Log.d(TAG, "invalidating token: " + t);
        if (!TextUtils.isEmpty(t))
            getAccountManager().invalidateAuthToken(Constants.ACCOUNT_TYPE, t);
    }

    public String peekAuthToken() {
        return getAccountManager().peekAuthToken(getSingleAccount(), Constants.AUTH_TOKEN_TYPE);
    }

    public AccountManagerFuture<Bundle> getAuthToken(final Activity activity) {
        return getAccountManager().getAuthToken(getSingleAccount(), Constants.AUTH_TOKEN_TYPE, null, activity, null, null);
    }

    public void clearPassword() {
        Account a = getSingleAccount();
        if (a == null)
            return;
        getAccountManager().clearPassword(a);
    }

    public OAuthToken blockingGetAuthToken() {
        String tkn = peekAuthToken();
        OAuthToken token = OAuthToken.from(tkn);
        if (token == null || token.isExpired()) {
            try {
                if (token != null)
                    invalidateToken(token.toString());
                tkn = getAccountManager().blockingGetAuthToken(getSingleAccount(), Constants.AUTH_TOKEN_TYPE, true);
                Log.d(TAG, StringUtil.format("authToken: %s", tkn));
                token = OAuthToken.from(tkn);
            }
            catch (OperationCanceledException e) {
                Log.e(TAG, "OperationCanceled", e);
            }
            catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }
            catch (AuthenticatorException e) {
                Log.e(TAG, "AuthenticatorException", e);
            }
        }
        return token;
    }
}
