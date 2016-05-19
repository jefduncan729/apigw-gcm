package com.axway.apigwgcm.accounts;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.api.ActivityCallback;
import com.axway.apigwgcm.api.CertValidationException;
import com.axway.apigwgcm.oauth.OAuthClient;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import okhttp3.Call;

/**
 * Created by su on 11/15/2014.
 */
public class AuthActivity extends AccountAuthenticatorActivity implements TextWatcher, View.OnClickListener {

    private static final String TAG = AuthActivity.class.getSimpleName();

    private EditText txtUser;
    private EditText txtPwd;
    private EditText txtHost;
    private EditText txtPort;
    private TextView txtMsg;
    private EditText txtCliId;
    private EditText txtCliSec;

    private Button btnLogin;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private boolean newAcct;
    private boolean noMas;
    private boolean confirmCreds;
    private boolean viewFilled;
    private BaseApp baseApp = BaseApp.getInstance();
    private Thread authThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.auth_activity);
        authThread = null;
        viewFilled = false;
        Intent i = getIntent();
        String action = i.getAction();
        String username = i.getStringExtra(Constants.EXTRA_ITEM_NAME);
        newAcct = TextUtils.isEmpty(username);
        confirmCreds = i.getBooleanExtra(Constants.EXTRA_ACTION, false);
        noMas = (!newAcct && Intent.ACTION_INSERT.equals(action));
        final Account acct = getAccountUtil().getAccount(username);
        progressBar = (ProgressBar)findViewById(android.R.id.progress);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (toolbar != null) {
            setupToolbar();
            setActionBar(toolbar);
        }
        txtCliId = (EditText)findViewById(R.id.client_id);
        txtCliSec = (EditText)findViewById(R.id.client_secret);
        txtUser = (EditText)findViewById(android.R.id.text1);
        txtPwd = (EditText)findViewById(android.R.id.text2);
        txtHost = (EditText)findViewById(R.id.gateway_host);
        txtPort = (EditText)findViewById(R.id.gateway_port);
        txtMsg = (TextView)findViewById(R.id.label01);
        btnLogin = (Button)findViewById(android.R.id.button1);
        txtUser.addTextChangedListener(this);
        txtPwd.addTextChangedListener(this);
        txtHost.addTextChangedListener(this);
        txtPort.addTextChangedListener(this);
        btnLogin.setOnClickListener(this);
        btnLogin.setEnabled(false);
        ViewGroup ctrHost = (ViewGroup)findViewById(R.id.container02);
        ViewGroup ctrCreds = (ViewGroup)findViewById(R.id.container03);
        View focus = null;
        int titleId = R.string.app_name;
        int btnTxtId = android.R.string.ok;
        if (noMas) {
            ctrCreds.setVisibility(View.GONE);
            ctrHost.setVisibility(View.GONE);
            txtMsg.setText(getString(R.string.single_acct_text));
            btnLogin.setEnabled(true);
            btnLogin.setTag(false);
            focus = btnLogin;
        }
        else {
            if (newAcct) {
                titleId = R.string.action_create_acct;
                btnTxtId = titleId;
                ctrHost.setVisibility(View.VISIBLE);
                ctrCreds.setVisibility(View.VISIBLE);
                txtMsg.setText(getString(R.string.welcome_text));
                btnLogin.setTag(true);
                focus = txtHost;
            }
            else {
                ctrHost.setVisibility(View.GONE);
                ctrCreds.setVisibility(View.VISIBLE);
                titleId = R.string.login;
                btnTxtId = titleId;
                btnLogin.setTag(true);
                if (acct != null) {
                    txtPwd.setText(getAccountMgr().getPassword(acct));
                    String[] parts = getAccountUtil().splitAccountName(username);
                    if (parts == null || parts.length != 3)
                        throw new IllegalStateException("account name is badly formatted: " + username);
                    txtMsg.setText(getString(R.string.login_text, parts[AccountUtil.NDX_USERNAME]));
                    txtHost.setText(parts[AccountUtil.NDX_HOST]);  //i.getStringExtra(Constants.EXTRA_GATEWAY_HOST));
                    txtPort.setText(parts[AccountUtil.NDX_PORT]);  //Integer.toString(i.getIntExtra(Constants.EXTRA_GATEWAY_PORT, 0)));
                    txtUser.setText(parts[AccountUtil.NDX_USERNAME]);
                    focus = (TextUtils.isEmpty(parts[AccountUtil.NDX_USERNAME]) ? txtUser : txtPwd);
                }
            }
        }
        setTitle(getString(titleId));
        btnLogin.setText(getString(btnTxtId));
        if (focus != null)
            focus.requestFocus();
        viewFilled = true;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (toolbar != null)
            toolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        if (toolbar != null)
            toolbar.setTitle(titleId);
    }

    protected void setupToolbar() {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (noMas)
            return true;
        menu.add(1, android.R.id.button1, 2, "demo");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.button1) {
            txtCliId.setText(Constants.API_GATEWAY_CLIENT_ID);
            txtCliSec.setText(Constants.API_GATEWAY_CLIENT_SECRET);
            txtHost.setText("10.71.100.177");
            txtPort.setText("8089");
            txtUser.setText("regadmin");
            txtPwd.setText("changeme");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (viewFilled)
            btnLogin.setEnabled(isValid(false));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == android.R.id.button1) {
            boolean canLogin = (Boolean)view.getTag();
            if (canLogin) {
                if (isValid(true)) {
                    startAuthTask();
                } else {
                    txtMsg.setText("Invalid credentials");
                }
            }
            else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void showProgress(boolean show) {
        if (progressBar == null)
            return;
        Log.d(TAG, "showProgress: " + Boolean.toString(show));
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (btnLogin != null)
            btnLogin.setEnabled(!show);
    }

    private boolean isValid(boolean fullValidation) {
        boolean rv = true;
        if (TextUtils.isEmpty(txtUser.getText().toString()) || TextUtils.isEmpty(txtPwd.getText().toString()) || TextUtils.isEmpty(txtHost.getText().toString()) || TextUtils.isEmpty(txtPort.getText().toString()) || TextUtils.isEmpty(txtCliId.getText().toString()) || TextUtils.isEmpty(txtCliSec.getText().toString()))
            rv = false;
/*
        if (rv && fullValidation) {
            ArrayList<String> accts = acctUtil.getAccountNames();
            if (accts != null) {
                for (String acct: accts) {
                    if (acct.startsWith(txtUser.getText().toString()+"@")) {
                        Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show();
                        rv = false;
                        break;
                    }
                }
            }
        }
*/
        Log.d(TAG, "isValid returning " + Boolean.toString(rv));
        return rv;
    }

    private void startAuthTask() {
        showProgress(true);
        Log.d(TAG, "starting auth task");
        final String id = txtCliId.getText().toString();
        final String secret = txtCliSec.getText().toString();
        baseApp.saveOAuthCreds(id, secret);
        final String user = txtUser.getText().toString();
        final String pwd = txtPwd.getText().toString();
        final String host = txtHost.getText().toString();
        final String p = txtPort.getText().toString();
        int port = Integer.parseInt(p);
        Log.d(TAG, "authTask.run: " + user + ", " + pwd + ", " + host + ", " + Integer.toString(port));
        String fullUser = getAccountUtil().buildAccountName(user, host, port);
        try {
            baseApp.oAuthClient().getAccessToken(fullUser, pwd, new AuthTokenCallback(this, fullUser));
        }
        catch (CertValidationException e) {
            final CertPath certPath = e.getCertPath();
            runOnUiThread(new CertPathRunnable(fullUser, certPath));
        }
    }

    private class AuthTokenCallback extends ActivityCallback {

        String user;

        public AuthTokenCallback(Activity activity, String user) {
            super(activity);
            this.user = user;
        }

        @Override
        public void onFailureResponse(Call call, IOException e) {
            super.onFailureResponse(call, e);
            baseApp.removeOAuthCreds();
            final CertPath certPath = BaseApp.certPathFromThrowable(e);
            if (certPath != null) {
                runOnUiThread(new CertPathRunnable(user, certPath));
                return;
            }
            authCancelled();
        }

        @Override
        protected void onSuccessResponse(int code, String msg, String body) {
            final JsonObject json = JsonUtil.getInstance().parseAsJsonObject(body);
            authComplete(OAuthToken.from(json));
        }
    }

    private class CertPathRunnable implements Runnable {

        private CertPath certPath;
        private String user;

        public CertPathRunnable(final String user, final CertPath certPath) {
            super();
            this.user = user;
            this.certPath = certPath;
        }

        @Override
        public void run() {
            certNotTrusted(this.user, certPath);
        }
    }

    private void authComplete(OAuthToken authToken) {
        boolean success = (authToken != null) && !TextUtils.isEmpty(authToken.getAccessToken());
        Log.d(TAG, "authComplete: " + Boolean.toString(success));
        showProgress(false);
        if (success) {
            if (confirmCreds)
                finishConfirmCreds(success, authToken);
            else
                finishLogin(authToken);
        }
        else {
            String msg = "Enter valid credentials";
            txtMsg.setText(msg);
        }
    }

    private Intent buildResult() {
        final String username = txtUser.getText().toString();
        final String host = txtHost.getText().toString();
        final int port = Integer.parseInt(txtPort.getText().toString());
        final Intent rv = new Intent();
        rv.putExtra(Constants.EXTRA_GATEWAY_HOST, host);
        rv.putExtra(Constants.EXTRA_GATEWAY_PORT, port);
        rv.putExtra(AccountManager.KEY_ACCOUNT_NAME, getAccountUtil().buildAccountName(username, host, port));   //username);
        rv.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        return rv;
    }

    private void finishConfirmCreds(boolean result, OAuthToken token) {
        Log.d(TAG, "finishConfirmCreds: " + Boolean.toString(result));
        final String password = txtPwd.getText().toString();
        final Intent intent = buildResult();
        final Account acct = new Account(intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), Constants.ACCOUNT_TYPE);
        getAccountMgr().setPassword(acct, password);
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void finishLogin(OAuthToken token) {
        Log.d(TAG, "finishLogin: " + token);
        final String password = txtPwd.getText().toString();
        final Intent intent = buildResult();
        final Account acct = new Account(intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), Constants.ACCOUNT_TYPE);
        final AccountManager mgr = getAccountMgr();
        if (newAcct) {
            mgr.addAccountExplicitly(acct, password, null);
        }
        else {
            mgr.setPassword(acct, password);
        }
        mgr.setAuthToken(acct, Constants.AUTH_TOKEN_TYPE, token.toString());
        intent.putExtra(Constants.EXTRA_NEW_ACCT, newAcct);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, token.toString());
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void authCancelled() {
        Log.d(TAG, "authCancelled");
        showProgress(false);
    }

    private void certNotTrusted(final String user, final CertPath cp) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Certificate c : cp.getCertificates()) {
            if ("X.509".equals(c.getType())) {
                X509Certificate c509 = (X509Certificate) c;
                sb.append("[").append(++i).append("]: ").append(c509.getSubjectDN().toString()).append("\n");
            }
        }
        sb.append("\n").append(getString(R.string.add_to_truststore));
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle(getString(R.string.cert_not_trusted))
                .setCancelable(true)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addCertsToTruststore(user, cp);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                    refreshFrag();
                    }
                });
        AlertDialog dlg = bldr.create();
        dlg.show();
    }

    public void addCertsToTruststore(final String user, final CertPath cp) {
        Log.d(TAG, "trusting cert");
        BaseApp.addTrustedCert(user, cp);
        startAuthTask();
        Log.i(TAG, "cert trusted");
    }
}
