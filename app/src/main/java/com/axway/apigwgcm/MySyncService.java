package com.axway.apigwgcm;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.triggers.EventTrigger;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.GcmUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.axway.apigwgcm.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 4/22/2016.
 */
public class MySyncService extends IntentService {
    public static final String TAG = MySyncService.class.getSimpleName();

    private ResultReceiver resRcvr;
    private JsonUtil jsonUtil = JsonUtil.getInstance();
    private ContentResolver resolver;

    public MySyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            extras = new Bundle();
        if (extras.containsKey(Intent.EXTRA_RESULT_RECEIVER)) {
            resRcvr = extras.getParcelable(Intent.EXTRA_RESULT_RECEIVER);
        }
        String a = intent.getAction();
        if (SyncAdapter.ACTION_SYNC_COMPLETE.equals(a)) {
            resolver = getContentResolver();
            performSync(extras);
        }
    }

    private void performSync(Bundle extras) {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connMgr.getActiveNetworkInfo();
        if (ni == null) {
            Log.d(TAG, "no network connection, halting");
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(Constants.KEY_SYNC_WIFI_ONLY, true) && ni.getType() != ConnectivityManager.TYPE_WIFI) {
            Log.d(TAG, "not a wi-fi connection, halting");
            return;
        }
        if (!ni.isConnectedOrConnecting()) {
            Log.d(TAG, "wi-fi connection problem, halting");
            return;
        }
        AccountUtil au = AccountUtil.getInstance(this);
        Account account = au.getSingleAccount();
        long lastSync = prefs.getLong(Constants.EXTRA_LAST_SYNC_TIME, 0);
        String u = extras.getString(Intent.EXTRA_UID);
        Uri uri = null;
        if (!TextUtils.isEmpty(u))
            uri = Uri.parse(u);
        StringBuilder sb = new StringBuilder();
        sb.append("lastSync=").append(lastSync);
        sb.append(", uri=").append(uri == null ? "null" : uri.toString());
        sb.append(", account=").append(account==null ? "null" : account.name);
        sb.append(", extras=").append(extras.isEmpty() ? "none" : extras.toString());
        sb.append(", authority=").append(DbHelper.CONTENT_AUTHORITY);
        Log.d(TAG, "onPerformSync: " + sb.toString());
        long now = System.currentTimeMillis();
        long dlt = now - lastSync;
        if (dlt < (3 * DateUtils.MINUTE_IN_MILLIS)) {
            Log.d(TAG, StringUtil.format("too soon to sync again: %d secs", dlt/1000));
            return;
        }
        Bundle gcmCfg = GcmUtil.loadSettings(prefs);
        boolean isSsl = gcmCfg.getBoolean(Constants.KEY_SERVICES_USE_SSL, false);
        int port = gcmCfg.getInt(Constants.KEY_SERVICES_PORT, 8080);
        String tkn = au.peekAuthToken();
        OAuthToken token = OAuthToken.from(tkn);
        if (token == null || token.isExpired()) {
            try {
                tkn = au.getAccountManager().blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true);
                Log.d(TAG, StringUtil.format("authToken: %s", tkn));
                token = OAuthToken.from(tkn);
            } catch (OperationCanceledException e) {
                Log.e(TAG, "OperationCanceled", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (AuthenticatorException e) {
                Log.e(TAG, "AuthenticatorException", e);
            }
        }
        if (token == null) {
            Log.e(TAG, "could not get auth token");
            return;
        }
        String[] parts = au.splitAccountName(account);
        String url = StringUtil.format("%s://%s:%d/gcm/triggers/%s", isSsl ? Constants.HTTPS_SCHEME : Constants.HTTP_SCHEME, parts[AccountUtil.NDX_HOST], port, parts[AccountUtil.NDX_USERNAME]);

        List<Long> doneIds = new ArrayList<>();
        List<Long> newIds = idsWithFlag(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.FLAG_NEW);
        Log.d(TAG, StringUtil.format("newIds: %d", newIds.size()));
        for (long id: newIds) {
            if (uploadRow(id, url, token))
                doneIds.add(id);
        }
        List<Long> updIds = idsWithFlag(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.FLAG_UPDATED);
        Log.d(TAG, StringUtil.format("updIds: %d", updIds.size()));
        for (long id: updIds) {
            if (uploadRow(id, url, token))
                doneIds.add(id);
        }
        markAll(doneIds, DbHelper.FLAG_INSYNC);

        if (checkForNew(url, token)) {
            prefs.edit().putLong(Constants.EXTRA_LAST_SYNC_TIME, System.currentTimeMillis()).apply();
        }

/*
        String u = null;
        Uri uri = null;
        Cursor c = null;
        int cnt = 0;
        List<Long> updatedIds = new ArrayList<Long>();
        if (extras != null && extras.containsKey(Intent.EXTRA_UID)) {
            u = extras.getString(Intent.EXTRA_UID);
            if (!TextUtils.isEmpty(u))
                uri = Uri.parse(u);
        }
        if (uri == null) {
            lastSync = prefs.getLong(Constants.EXTRA_LAST_SYNC_TIME, 0);
            int flag = (lastSync == 0 ? DbHelper.FLAG_INSYNC : DbHelper.FLAG_NEW);
            String sel = DbHelper.TriggerColumns.FLAG + " >= ?";
            String[] selArgs = new String[] { Integer.toString(flag)};
            Log.d(TAG, "using query: " + sel);
            c = resolver.query(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.TriggerColumns.DEF_PROJECTION, sel, selArgs, DbHelper.TriggerColumns.DEF_SORT_ORDER);
        }
        else {
            Log.d(TAG, "using uri: " + u);
            c = resolver.query(uri, DbHelper.TriggerColumns.DEF_PROJECTION, null, null, null);
        }

        if (c != null) {
            cnt = c.getCount();
            while (c.moveToNext()) {
                if (uploadRow(url, token, c)) {
                    updatedIds.add(c.getLong(DbHelper.TriggerColumns.NDX_ID));
                }
            }
            c.close();
        }
        int updCnt = 0;
        if (updatedIds.size() > 0) {
            StringBuilder inClause = new StringBuilder(DbHelper.TriggerColumns._ID + " IN (");
            String[] args = new String[updatedIds.size()];
            for (int i = 0; i < updatedIds.size(); i++) {
                if (i > 0)
                    inClause.append(", ");
                inClause.append("?");
                args[i] = Long.toString(updatedIds.get(i));
            }
            inClause.append(")");
            final ContentValues values = new ContentValues();
            values.put(DbHelper.TriggerColumns.FLAG, DbHelper.FLAG_INSYNC);
            values.put(Constants.EXTRA_FROM_SYNC, true);
            updCnt = resolver.update(DbHelper.TriggerColumns.CONTENT_URI, values, inClause.toString(), args);
        }
        Log.d(TAG, "actualCount: " + Integer.toString(cnt) + ", updateCount: " + Integer.toString(updCnt));
        updatedIds = null;

        resolver.notifyChange(DbHelper.TriggerColumns.CONTENT_URI, null, false);
*/
//        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
//        Intent i = new Intent(SyncAdapter.ACTION_SYNC_COMPLETE);
//        i.addCategory(SyncAdapter.CATEGORY_SYNC);
//        lbm.sendBroadcast(i);
        if (resRcvr != null) {
            resRcvr.send(42, extras);
        }
        Log.d(TAG, "sync complete, broadcast sent");

    }
    private List<Long> idsWithFlag(final Uri uri, final int state) {
        List<Long> rv = new ArrayList<>();
        String sel = StringUtil.format("%s = ?", DbHelper.CommonColumns.FLAG);
        String[] selArgs = { Integer.toString(state) };
        Cursor c = resolver.query(uri, DbHelper.TriggerColumns.DEF_PROJECTION, sel, selArgs, DbHelper.TriggerColumns.DEF_SORT_ORDER);
        int cnt = 0;
        if (c != null) {
            cnt = c.getCount();
            while (c.moveToNext()) {
                rv.add(c.getLong(DbHelper.TriggerColumns.NDX_ID));
            }
            c.close();
        }
        return rv;
    }

    private int markAll(final List<Long> ids, final int state) {
        if (ids == null || ids.size() == 0)
            return 0;
        String[] selArgs = new String[ids.size()];
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ids.size(); i++) {
            selArgs[i] = Long.toString(ids.get(i));
            if (i > 0)
                sb.append(", ");
            sb.append("?");
        }
        sb.append(")");
        String sel = StringUtil.format("%s IN %s", DbHelper.TriggerColumns._ID, sb.toString());
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.TriggerColumns.FLAG, state);
        int rv = resolver.update(DbHelper.TriggerColumns.CONTENT_URI, cv, sel, selArgs);
        return rv;
    }
    private boolean checkForNew(final String url, final OAuthToken token) {
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        JsonArray res = null;
        String s = null;
        Response resp = null;
        try {
            resp = BaseApp.getInstance().executeRequest(req);
            s = BaseApp.getInstance().consumeStringResponse(resp);
//            if (resp.isSuccessful()) {
//                s = resp.body().string();
//            }
        }
        catch (IOException e) {
            Log.d(TAG, "IOException", e);
            return false;
        }
//        finally {
//            if (resp != null)
//                resp.body().close();
//        }
        res = JsonUtil.getInstance().parseAsJsonArray(s);
        if (res == null)
            return false;
        int updCnt = 0;
        int insCnt = 0;
        for (int i = 0; i < res.size(); i++) {
            JsonObject remote = res.get(i).getAsJsonObject();
            if (remote == null || !remote.has("id"))
                continue;
            long id = Long.parseLong(remote.get("id").getAsString());
            JsonObject local = findTrigger(id);
            if (local == null) {
                insertTrigger(remote);
                insCnt++;
            }
            else if (!trigEquals(local, remote)) {
                updateTrigger(remote);
                updCnt++;
            }
        }
        Log.d(TAG, StringUtil.format("updated: %d, added: %d", updCnt, insCnt));
        return (updCnt > 0 || insCnt > 0);
    }

    private boolean trigEquals(final JsonObject local, final JsonObject remote) {
        if (local == null || remote == null)
            return false;
        if (!jsonUtil.compareStr(local, remote, "name")) {
            Log.d(TAG, "name differs");
            return false;
        }
        if (!jsonUtil.compareStr(local, remote, "expression")) {
            Log.d(TAG, "expression differs");
            return false;
        }
        if (!jsonUtil.compareInt(local, remote, "trigger_type")) {
            Log.d(TAG, "trigger_type differs");
            return false;
        }
        if (!jsonUtil.compareInt(local, remote, "status")) {
            Log.d(TAG, "status differs");
            return false;
        }
        if (!jsonUtil.compareInt(local, remote, "flag")) {
            Log.d(TAG, "flag differs");
            return false;
        }
        Log.d(TAG, "local and remote are equal");
        return true;
    }

    private JsonObject findTrigger(long id) {
        Uri uri = ContentUris.withAppendedId(DbHelper.TriggerColumns.CONTENT_URI, id);
        Cursor c = resolver.query(uri, DbHelper.TriggerColumns.DEF_PROJECTION, null, null, null);
        if (c == null) {
            Log.d(TAG, StringUtil.format("Trigger not found: %d", id));
            return null;
        }
        JsonObject rv = null;
        if (c.moveToFirst()) {
            rv = trigJson(c);
        }
        c.close();
        Log.d(TAG, StringUtil.format("findTrigger: %s,  %s", uri, rv == null ? "" : rv.toString()));
        return rv;
    }

    private JsonObject trigJson(Cursor c) {
        if (c == null)
            return null;
        JsonObject json = new JsonObject();
        json.addProperty("id", c.getLong(DbHelper.TriggerColumns.NDX_ID));
        json.addProperty("flag", c.getInt(DbHelper.TriggerColumns.NDX_FLAG));
        json.addProperty("status", c.getInt(DbHelper.TriggerColumns.NDX_STATUS));
        json.addProperty("name", c.getString(DbHelper.TriggerColumns.NDX_NAME));
        json.addProperty("expression", c.getString(DbHelper.TriggerColumns.NDX_EXPR));
        json.addProperty("trigger_type", c.getInt(DbHelper.TriggerColumns.NDX_TYPE));
        json.addProperty("priority", 5);    //c.getInt(DbHelper.TriggerColumns.NDX_PRIORITY));
        return json;
    }

    private ContentValues trigValues(JsonObject obj, boolean isNew) {
        ContentValues cv = new ContentValues();
        if (isNew && obj.has("id"))
            cv.put(DbHelper.TriggerColumns._ID, jsonUtil.coerceLong(obj, "id"));
        if (obj.has("name"))
            cv.put(DbHelper.TriggerColumns.NAME, obj.get("name").getAsString());
        if (obj.has("expression"))
            cv.put(DbHelper.TriggerColumns.EXPRESSION, obj.get("expression").getAsString());
        cv.put(DbHelper.TriggerColumns.FLAG, jsonUtil.coerceInt(obj, "flag", DbHelper.FLAG_INSYNC)); //obj.get("flag").getAsInt());
        cv.put(DbHelper.TriggerColumns.STATUS, jsonUtil.coerceInt(obj, "status"));
        cv.put(DbHelper.TriggerColumns.TYPE, jsonUtil.coerceInt(obj, "trigger_type", EventTrigger.REQUEST_TRIGGER));
        long now = System.currentTimeMillis();
        cv.put(DbHelper.TriggerColumns.MODIFY_DATE, now);
        if (isNew)
            cv.put(DbHelper.TriggerColumns.CREATE_DATE, now);
        cv.put(Constants.EXTRA_FROM_SYNC, true);
        Log.d(TAG, StringUtil.format("trigValues: %s", cv.toString()));
        return cv;
    }

    private Uri insertTrigger(JsonObject obj) {
        if (obj == null)
            return null;
        Log.d(TAG, StringUtil.format("insertTrigger %s", obj));
        ContentValues cv = trigValues(obj, true);
        Uri uri = resolver.insert(DbHelper.TriggerColumns.CONTENT_URI, cv);
        return uri;
    }

    private void updateTrigger(JsonObject obj) {
        if (obj == null || !obj.has("id"))
            return;
        Log.d(TAG, StringUtil.format("updateTrigger %s", obj));
        ContentValues cv = trigValues(obj, false);
        Uri uri = ContentUris.withAppendedId(DbHelper.TriggerColumns.CONTENT_URI, obj.get("id").getAsLong());
        resolver.update(uri, cv, null, null);
    }

    private boolean uploadRow(final long id, final String url, final OAuthToken token) {
        JsonObject json = null;
        Uri uri = ContentUris.withAppendedId(DbHelper.TriggerColumns.CONTENT_URI, id);
        Cursor c = resolver.query(uri, DbHelper.TriggerColumns.DEF_PROJECTION, null, null, DbHelper.TriggerColumns.DEF_SORT_ORDER);
        if (c != null) {
            if (c.moveToNext()) {
//            int flag = c.getInt(DbHelper.TriggerColumns.NDX_FLAG);
//            if (flag == DbHelper.FLAG_INSYNC)
//                flag = DbHelper.FLAG_NEW;
                json = trigJson(c);
            }
            c.close();
        }
        if (json == null)
            return false;
        json.remove("flag");
        json.addProperty("flag", DbHelper.FLAG_INSYNC);
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .method("POST", RequestBody.create(MediaType.parse("application/json"), json.toString()))
                .build();
        Response resp = null;
        try {
            resp = BaseApp.getInstance().executeRequest(req);
            if (resp.isSuccessful()) {
                return true;
            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        finally {
            if (resp != null)
                resp.body().close();
        }
        return false;
    }
}
