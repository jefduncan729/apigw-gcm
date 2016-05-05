package com.axway.apigwgcm.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.fragment.GcmRegFragment;
import com.axway.apigwgcm.oauth.GetTokenTask;
import com.axway.apigwgcm.oauth.OAuthClient;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.GcmUtil;
import com.axway.apigwgcm.util.SafeAsyncTask;
import com.axway.apigwgcm.util.StringUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 11/15/2014.
 */
public class RegistrationActivity extends BaseActivity implements
        GcmRegFragment.Callbacks {

    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private static final int REQ_GCM_REGISTER = REQ_BASE + 1;
    private static final int REQ_GCM_UNREGISTER = REQ_BASE + 2;
    private static final int REQ_RESOLVE_PLAY_SVCS = REQ_BASE + 3;

    private static final int MSG_UPDATE_FRAG = REQ_BASE + 101;

    private GoogleCloudMessaging gcm;
    private RegisterTask regTask;
    private UnregisterTask unregTask;
    private AccountUtil acctUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acctUtil = AccountUtil.getInstance(this);
        setContentView(R.layout.tb_frame);
        if (TextUtils.isEmpty(GcmUtil.getRegistrationId(getPrefs())))
            setTitle(getString(R.string.new_reg_title));
        else
            setTitle(getString(R.string.mod_reg_title));
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        }
        else {
            gcm = null;
        }
        showGcmRegFrag();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void register(final Bundle data) {
        Log.d(TAG, "GCM registration callback");
        if (regTask != null)
            return;
        regTask = new RegisterTask(this, data.getBoolean(Constants.EXTRA_NEW_ACCT, false));
        regTask.execute(data);
    }

    @Override
    public void unregister() {
        confirmUnregister();
    }

    protected void confirmUnregister() {
        String msg = getString(R.string.confirm_msg, "unregister this device (you may re-register at any time)");
        confirmDialog(msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performUnregister();
            }
        });
    }

    private void performUnregister() {
        Log.d(TAG, "GCM unregistration");
        if (unregTask != null)
            return;
        unregTask = new UnregisterTask(this);
        unregTask.execute();
    }

    protected String makeBaseUrl(final Bundle bnd) {
        StringBuilder sb = new StringBuilder();
        if (bnd.getBoolean(Constants.KEY_SERVICES_USE_SSL, false))
            sb.append(Constants.HTTPS_SCHEME);
        else
            sb.append(Constants.HTTP_SCHEME);
        sb.append("://");
        String[] parts = acctUtil.splitAccountName(acctUtil.getSingleAccount());
        if (parts != null && parts.length == 3)
            sb.append(parts[AccountUtil.NDX_HOST]);
        sb.append(":").append(bnd.getInt(Constants.KEY_SERVICES_PORT));
        return sb.toString();
    }

    private void unregistrationComplete(Boolean ok) {
        unregTask = null;
        showProgressBar(false);
        invalidateOptionsMenu();
        if (ok) {
            GcmUtil.removeRegistrationId(getPrefs());
            showToast("Unregistration complete");
            finish();
        }
        else {
            showToast("Something went wrong");
        }
    }

    private class UnregisterTask extends SafeAsyncTask<Void, Void, Boolean> {
        public UnregisterTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected Boolean run(Void... args) {
            if (getGcm() == null)
                return false;
            Response resp = null;
            OAuthToken tkn = acctUtil.blockingGetAuthToken();
            if (tkn == null) {
                Log.d(TAG, "could not get auth token");
                return false;
            }
            String token = tkn.getAccessToken();
            final Account acct = acctUtil.getSingleAccount();
            String[] parts = acctUtil.splitAccountName(acct);
            String user = parts == null ? "" : parts[0];
            String id = GcmUtil.getRegistrationId(getPrefs());
            if (TextUtils.isEmpty(id))
                return false;
            boolean rv = false;
            Bundle gcmCfg = GcmUtil.loadSettings(getPrefs());
            Request.Builder bldr = new Request.Builder();
            try {
                JsonObject json = new JsonObject();
                json.addProperty("reg_id", id);
                String u = makeBaseUrl(gcmCfg) + "/gcm/unregister";
                bldr.header("Authorization", "Bearer " + token);
                bldr.method("POST", RequestBody.create(MediaType.parse("application/json"), json.toString()));
                bldr.url(u);
                Request req = bldr.build();
                resp = baseApp.executeRequest(req);
                if (resp.isSuccessful()) {
                    getGcm().unregister();
//                    InstanceID.getInstance(baseApp.getBaseContext()).deleteInstanceID();
                    rv = true;
                }
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            finally {
                if (resp != null)
                    resp.body().close();
            }
            return rv;
        }

        @Override
        protected void doPreExecute() {
            showProgressBar(true);
        }

        @Override
        protected void doPostExecute(Boolean v) {
            unregistrationComplete(v);
        }

        @Override
        protected void doOnCancelled(Boolean v) {
            unregistrationComplete(false);
        }
    }

    private class RegisterTask extends SafeAsyncTask<Bundle, Void, Bundle> {

        private boolean isNew;

        public RegisterTask(Context ctx, boolean isNew) {
            super(ctx);
            this.isNew = isNew;
        }

        @Override
        protected Bundle run(Bundle... args) {
            if (getGcm() == null)
                return null;
            Bundle b = args[0];
            Bundle rv = new Bundle();
            rv.putAll(b);
            String id = null;
            Response resp = null;
            OAuthToken tkn = acctUtil.blockingGetAuthToken();
            if (tkn == null) {
                Log.d(TAG, "could not get auth token");
                return null;
            }
            String token = tkn.getAccessToken();
            final Account acct = acctUtil.getSingleAccount();
            String[] parts = acctUtil.splitAccountName(acct);
            String user = parts == null ? "" : parts[0];
            Request.Builder bldr = new Request.Builder();
//            InstanceID iid = InstanceID.getInstance(baseApp.getBaseContext());
            try {
                if (isNew) {
                    id = getGcm().register(Constants.GCM_PROJECT_ID);
//                    id = iid.getId();
//                    String iid_tkn = iid.getToken(Constants.GCM_PROJECT_ID, "GCM");
                }
                else {
                    id = GcmUtil.getRegistrationId(getPrefs());
                }
                JsonObject json = new JsonObject();
                json.addProperty("reg_id", id);
                json.addProperty("username", user);
                json.addProperty("send_alerts", (rv.getBoolean(Constants.KEY_GCM_ALERTS, false) ? 1 : 0));
                json.addProperty("send_commands", (rv.getBoolean(Constants.KEY_GCM_COMMANDS, false) ? 1 : 0));
                json.addProperty("send_events", (rv.getBoolean(Constants.KEY_GCM_EVENTS, false) ? 1 : 0));
//        json.addProperty("send_messages", (prefs.isSendMessages() ? 1 : 0));
//                    req.setJson(json);
                String u = makeBaseUrl(rv) + "/gcm/register";
                bldr.header("Authorization", "Bearer " + token);
                bldr.method("POST", RequestBody.create(MediaType.parse("application/json"), json.toString()));
                bldr.url(u);
                Request req = bldr.build();
                resp = baseApp.executeRequest(req);
                if (resp.isSuccessful()) {
                    Log.d(TAG, StringUtil.format("regId: %s", id));
                }
                else {
                    if (isNew) {
                        getGcm().unregister();
//                        iid.deleteInstanceID();
                        GcmUtil.removeRegistrationId(getPrefs());
                        id = null;
                    }
                }
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            finally {
                if (resp != null)
                    resp.body().close();
            }
            if (id != null)
                rv.putString(Constants.KEY_REGISTRATION_ID, id);
            return rv;
        }

        @Override
        protected void doPreExecute() {
            showProgressBar(true);
        }

        @Override
        protected void doPostExecute(Bundle b) {
            registrationComplete(b);
        }

        @Override
        protected void doOnCancelled(Bundle b) {
            registrationCancelled();
        }
    }

    protected void registrationCancelled() {
        Log.d(TAG, "registration operation cancelled");
        regTask = null;
        showToast("registration cancelled");
        end(false);
    }

    protected void registrationComplete(final Bundle b) {
        regTask = null;
        boolean saved = false;
        if (b != null && !b.isEmpty()) {
            saved = GcmUtil.saveSettings(getPrefs(), b);
            if (saved && b.getBoolean(Constants.EXTRA_NEW_ACCT, false))
                GcmUtil.storeRegistrationId(getPrefs(), b.getString(Constants.KEY_REGISTRATION_ID));
            Log.d(TAG, "registration operation complete: " + b.toString());
        }
        invalidateOptionsMenu();
        String msg = "Registration complete";
        if (!saved) {
            msg = "Registration failed; try again later";
        }
        showToast(msg);
        end(saved);
    }

    private void end(boolean startMain) {
        finish();
        if (startMain) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    private void showGcmRegFrag() {
        Log.d(TAG, "showGcmRegFrag");
        GcmRegFragment frag = new GcmRegFragment(); //.newInstance(getRegistrationId(), getAccount(), getPrefs());
        Bundle args = new Bundle();
        String regId = GcmUtil.getRegistrationId(getPrefs());
        if (!TextUtils.isEmpty(regId))
            args.putString(Constants.KEY_REGISTRATION_ID, regId);
        Account acct = acctUtil.getSingleAccount();
        if (acct != null)
            args.putString(AccountManager.KEY_ACCOUNT_NAME, acct.name);
        args.putBundle(Constants.KEY_GCM_PREFS, GcmUtil.loadSettings(getPrefs()));
        frag.setArguments(args);
        replaceFragment(R.id.container01, frag, Constants.TAG_SINGLE_PANE);
    }

    protected GoogleCloudMessaging getGcm() {
        return gcm;
    }

    protected boolean checkPlayServices() {
        int res = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (res != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(res)) {
                GooglePlayServicesUtil.getErrorDialog(res, this, REQ_RESOLVE_PLAY_SVCS).show();
            }
            else {
                Log.d(TAG, "GooglePlayServices: device not supported");
            }
            return false;
        }
        return true;
    }

    @Override
    protected boolean navIsBack() {
        return true;
    }
}
