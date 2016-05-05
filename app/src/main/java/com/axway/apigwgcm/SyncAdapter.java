package com.axway.apigwgcm;

import android.accounts.Account;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.db.DemoProvider;
import com.axway.apigwgcm.oauth.OAuthClient;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.triggers.EventTrigger;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.GcmUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.axway.apigwgcm.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 12/27/2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    public static final String ACTION_SYNC_COMPLETE = "sync_complete";
    public static final String CATEGORY_SYNC = "sync";

    private ContentResolver resolver;
    private JsonUtil jsonUtil = JsonUtil.getInstance();
    private BaseApp baseApp = BaseApp.getInstance();

    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        resolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        ConnectivityManager connMgr = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connMgr.getActiveNetworkInfo();
        if (ni == null) {
            Log.d(TAG, "no network connection, halting");
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(Constants.KEY_SYNC_WIFI_ONLY, true) && ni.getType() != ConnectivityManager.TYPE_WIFI) {
            Log.d(TAG, "not a wi-fi connection, halting");
            return;
        }
        if (!ni.isConnectedOrConnecting()) {
            Log.d(TAG, "wi-fi connection problem, halting");
            return;
        }
        AccountUtil au = AccountUtil.getInstance(getContext());
        long lastSync = prefs.getLong(Constants.EXTRA_LAST_SYNC_TIME, 0);
        String u = extras.getString(Intent.EXTRA_UID);
        Uri uri = null;
        if (!TextUtils.isEmpty(u))
            uri = Uri.parse(u);
        StringBuilder sb = new StringBuilder();
        sb.append("lastSync=").append(lastSync);
        sb.append(", uri=").append(uri == null ? "null" : uri.toString());
        sb.append(", account=").append(account == null ? "null" : account.name);
        sb.append(", extras=").append(extras.isEmpty() ? "none" : extras.toString());
        sb.append(", authority=").append(DbHelper.CONTENT_AUTHORITY);
        Log.d(TAG, "onPerformSync: " + sb.toString());
        long now = System.currentTimeMillis();
        long dlt = now - lastSync;
        if (dlt < DateUtils.MINUTE_IN_MILLIS) {
            Log.d(TAG, StringUtil.format("too soon to sync again: %d secs", dlt / 1000));
            return;
        }

        OAuthToken token = au.blockingGetAuthToken();
        if (token == null) {
            Log.e(TAG, "could not get auth token");
            return;
        }

        Bundle gcmCfg = GcmUtil.loadSettings(prefs);
        boolean isSsl = gcmCfg.getBoolean(Constants.KEY_SERVICES_USE_SSL, false);
        int port = gcmCfg.getInt(Constants.KEY_SERVICES_PORT, 8080);
        String[] parts = au.splitAccountName(account);
        String url = StringUtil.format("%s://%s:%d/gcm/triggers/%s", isSsl ? Constants.HTTPS_SCHEME : Constants.HTTP_SCHEME, parts[AccountUtil.NDX_HOST], port, parts[AccountUtil.NDX_USERNAME]);

        //delete rows marked as deleted from server
        List<Long> delIds = deleteDeleted(url, token);
//        syncResult.stats.numDeletes = delIds.size();

        //upload new and updated rows from local database to server
        List<Long> chgd = new ArrayList<>();
        List<Long> newIds = uploadNew(url, token);
        List<Long> updIds = uploadChanged(url, token);
        chgd.addAll(newIds);
        chgd.addAll(updIds);
        syncResult.stats.numInserts += newIds.size();
        syncResult.stats.numUpdates += updIds.size();
        markAll(chgd, DbHelper.FLAG_INSYNC);

        //now see if any items on the server need to be updated
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        String s = null;
        try {
            Response resp = baseApp.executeRequest(req);
            s = baseApp.consumeStringResponse(resp);
        }
        catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        JsonArray res = jsonUtil.parseAsJsonArray(s);
        try {
            updateLocalData(chgd, res, syncResult);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        } catch (OperationApplicationException e) {
            Log.e(TAG, "OperationApplicationException", e);
        }

        prefs.edit().putLong(Constants.EXTRA_LAST_SYNC_TIME, System.currentTimeMillis()).apply();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        Intent i = new Intent(ACTION_SYNC_COMPLETE);
        i.addCategory(CATEGORY_SYNC);
        i.putExtra("syncResult", syncResult);
        lbm.sendBroadcast(i);
        Log.d(TAG, "sync complete, broadcast sent");
    }

    private List<Long> deleteDeleted(String url, OAuthToken token) {
        List<Long> ids = idsWithFlag(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.FLAG_DELETED);
        Log.d(TAG, String.format("deleting %d rows from server", ids.size()));
        List<Long> chgd = new ArrayList<>();
        for (long id: ids) {
            if (deleteRow(id, url, token)) {
                chgd.add(id);
            }
        }
        return chgd;
    }
    private List<Long> uploadNew(String url, OAuthToken token) {
        List<Long> ids = idsWithFlag(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.FLAG_NEW);
        Log.d(TAG, String.format("inserting %d rows on server", ids.size()));
        List<Long> chgd = new ArrayList<>();
        for (long id: ids) {
            if (uploadRow(true, id, url, token)) {
                chgd.add(id);
            }
        }
        return chgd;
    }

    private List<Long> uploadChanged(String url, OAuthToken token) {
        List<Long> ids = idsWithFlag(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.FLAG_UPDATED);
        Log.d(TAG, String.format("updating %d rows on server", ids.size()));
        List<Long> chgd = new ArrayList<>();
        for (long id: ids) {
            if (uploadRow(false, id, url, token)) {
                chgd.add(id);
            }
        }
        return chgd;
    }

    private void updateLocalData(final List<Long> chgdIds, final JsonArray array, final SyncResult result) throws RemoteException, OperationApplicationException {
        if (array == null) {
            return;
        }
        Cursor c = resolver.query(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.TriggerColumns.DEF_PROJECTION, null, null, DbHelper.TriggerColumns.DEF_SORT_ORDER);
        if (c == null)
            return;
        Map<Long, JsonObject> remote = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject jt = array.get(i).getAsJsonObject();
            remote.put(jt.get("id").getAsLong(), jt);
        }
        Log.d(TAG, String.format("numRemote: %d, numLocal: %d", remote.size(), c.getCount()));
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        while (c.moveToNext()) {
            result.stats.numEntries++;
            long id = c.getLong(DbHelper.TriggerColumns.NDX_ID);
            if (chgdIds != null && chgdIds.contains(id)) {
                //this row should be in sync already
                remote.remove(id);
                continue;
            }
            JsonObject rt = remote.get(id);
            if (rt == null) {
                //entry doesn't exist, remove from db
                Uri deleteUri = DbHelper.TriggerColumns.CONTENT_URI.buildUpon().appendPath(Long.toString(id))
                        .appendQueryParameter(Constants.EXTRA_FROM_SYNC, Boolean.toString(true))
                        .build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                result.stats.numDeletes++;

            }
            else {
                //entry exists in db, remove from map, then check if it's dirty
                remote.remove(id);
                JsonObject ct = trigJson(c);
                if (trigEquals(ct, rt)) {
                    //no update
                }
                else {
                    Uri updateUri = DbHelper.TriggerColumns.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
                    Log.i(TAG, "Scheduling update: " + updateUri);
                    result.stats.numUpdates++;
                    batch.add(ContentProviderOperation.newUpdate(updateUri)
                            .withValues(trigValues(rt, false))
                            .build());
                }
            }
        }
        c.close();

        //any remaining remote items must be new
        for (JsonObject rt: remote.values()) {
            Log.i(TAG, String.format("Scheduling insert: entry_id=%d", rt.get("id").getAsLong()));
            batch.add(ContentProviderOperation.newInsert(DbHelper.TriggerColumns.CONTENT_URI)
                    .withValues(trigValues(rt, true))
                    .build());
        }
        if (batch.size() > 0) {
            Log.i(TAG, String.format("Merge solution ready. Applying batch update. entries: %d, updates: %d, inserts: %d, deletes: %d", result.stats.numEntries, result.stats.numUpdates, result.stats.numInserts, result.stats.numDeletes));
            resolver.applyBatch(DbHelper.CONTENT_AUTHORITY, batch);
        }
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
        cv.put(Constants.EXTRA_FROM_SYNC, true);
        int rv = resolver.update(DbHelper.TriggerColumns.CONTENT_URI, cv, sel, selArgs);
        Log.d(TAG, String.format("markAll: %d updated", rv));
        return rv;
    }

    private boolean checkForNew(final String url, final OAuthToken token) {
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token.getAccessToken())
                .build();
        String s = null;
        try {
            Response resp = baseApp.executeRequest(req);
            s = baseApp.consumeStringResponse(resp);
        }
        catch (IOException e) {
            Log.d(TAG, "IOException", e);
            return false;
        }
        JsonArray res = jsonUtil.parseAsJsonArray(s);
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
        cv.put(DbHelper.TriggerColumns.NAME, jsonUtil.coerceString(obj, "name", ""));
        cv.put(DbHelper.TriggerColumns.EXPRESSION, jsonUtil.coerceString(obj, "expression", ""));
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

    private boolean uploadRow(final boolean isNew, final long id, final String url, final OAuthToken token) {
        JsonObject json = null;
        Uri uri = ContentUris.withAppendedId(DbHelper.TriggerColumns.CONTENT_URI, id);
        Cursor c = resolver.query(uri, DbHelper.TriggerColumns.DEF_PROJECTION, null, null, DbHelper.TriggerColumns.DEF_SORT_ORDER);
        if (c != null) {
            if (c.moveToNext()) {
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
        boolean rv = false;
        try {
            resp = baseApp.executeRequest(req);
            if (resp.isSuccessful()) {
                baseApp.consumeStringResponse(resp);
                rv = true;
            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        return rv;
    }

    private boolean deleteRow(final long id, final String url, final OAuthToken token) {
        JsonObject json = null;
        Uri uri = ContentUris.withAppendedId(DbHelper.TriggerColumns.CONTENT_URI, id);
        Cursor c = resolver.query(uri, DbHelper.TriggerColumns.DEF_PROJECTION, null, null, DbHelper.TriggerColumns.DEF_SORT_ORDER);
        if (c != null) {
            if (c.moveToNext()) {
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
                .method("DELETE", RequestBody.create(MediaType.parse("application/json"), json.toString()))
                .build();
        Response resp = null;
        boolean rv = false;
        try {
            resp = baseApp.executeRequest(req);
            if (resp.isSuccessful()) {
                baseApp.consumeStringResponse(resp);
                rv = true;
            }
        }
        catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        return rv;
    }
}
