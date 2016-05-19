package com.axway.apigwgcm;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.util.JsonUtil;
import com.axway.apigwgcm.util.StringUtil;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by su on 5/5/2016.
 */
public class GcmCommandService extends BaseIntentService {
    private static final String TAG = GcmCommandService.class.getSimpleName();

    public GcmCommandService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        Uri uri = intent.getData();
        if (uri == null) {
            Log.d(TAG, "intent has no uri");
            return;
        }
        Log.d(TAG, StringUtil.format("command uri: %s", uri));
        processCmd(uri);
    }

    protected void processCmd(final Uri uri) {
        Cursor c = getContentResolver().query(uri, DbHelper.CommandColumns.DEF_PROJECTION, null, null, null);
        if (c == null) {
            Log.d(TAG, "query error");
            return;
        }
        String cmd = null;
        String p = null;
        String ackUrl = null;
        long id = -1;
        if (c.moveToFirst()) {
            id = c.getLong(DbHelper.CommandColumns.NDX_ID);
            cmd = c.getString(DbHelper.CommandColumns.NDX_SUBJECT);
            p = c.getString(DbHelper.CommandColumns.NDX_MESSAGE);
            ackUrl = c.getString(DbHelper.CommandColumns.NDX_ACK_URL);
            Log.d(TAG, StringUtil.format("cmd: %d, %s, params: %s, ack: %s", id, cmd, p, ackUrl));
        }
        c.close();
        String res = executeCmd(id, cmd, p);
        JsonObject j = new JsonObject();
        j.addProperty("id", id);
        j.addProperty("command", cmd);
        j.addProperty("params", p);
        j.addProperty("ack_url", ackUrl);
        j.addProperty("response", res);

        boolean acked = false;
        if (TextUtils.isEmpty(ackUrl) || "[invalid field]".equals(ackUrl)) {
            Log.d(TAG, "no ack_url specified");
            ackUrl = "";
            acked = true;
        }
        if (!acked && (haveWifiNetwork() || haveMobileNetwork())) {
            String t = acctUtil.peekAuthToken();
            OAuthToken tkn = OAuthToken.from(jsonUtil.parseAsJsonObject(t));
            if (tkn == null || tkn.isExpired())
                tkn = acctUtil.blockingGetAuthToken();
            if (tkn == null || tkn.isExpired()) {
                return;
            }
            Request req = new Request.Builder()
                    .url(ackUrl)
                    .addHeader("Authorization", "Bearer " + tkn.getAccessToken())
                    .method("POST", RequestBody.create(MediaType.parse("application/json"), j.toString()))
                    .build();
            Response resp = null;
            try {
                resp = baseApp.executeRequest(req);
                if (resp.isSuccessful()) {
                    acked = true;
                    baseApp.consumeStringResponse(resp);
                }
            } catch (IOException e) {
                Log.e(TAG, "in cmd_ack", e);
            }
        }
//        Uri updUri = ContentUris.withAppendedId(DbHelper.CommandColumns.CONTENT_URI, id);
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.CommandColumns.STATUS, (acked ? DbHelper.STATUS_ACKED : DbHelper.STATUS_DISABLED));
        cv.put(DbHelper.CommandColumns.MODIFY_DATE, System.currentTimeMillis());
        getContentResolver().update(uri, cv, null, null);
    }

    private String executeCmd(long id, String cmd, String params) {
        Log.d(TAG, StringUtil.format("executeCmd: %d, %s, %s", id, cmd, params));
        if (TtsService.ACTION_SPEAK.equals(cmd)) {
            Intent i = new Intent(this, TtsService.class);
            i.putExtra("params", params);
            i.setAction(TtsService.ACTION_SPEAK);
            startService(i);
            return "tts service started";
        }
        return "empty response";
    }

    private boolean ackCommand(JsonObject j) {
//        JsonObject j = new JsonObject();
//        j.addProperty("id", id);
//        j.addProperty("command", cmd);
//        j.addProperty("params", p);
//        j.addProperty("response", res);
//        j.addProperty("ack_url", ackUrl);
        String t = acctUtil.peekAuthToken();
        OAuthToken tkn = OAuthToken.from(jsonUtil.parseAsJsonObject(t));
        if (tkn == null || tkn.isExpired())
            tkn = acctUtil.blockingGetAuthToken();
        if (tkn == null || tkn.isExpired()) {
            return false;
        }
        Request req = new Request.Builder()
                .url(j.get("ack_url").getAsString())
                .addHeader("Authorization", "Bearer " + tkn.getAccessToken())
                .method("POST", RequestBody.create(MediaType.parse("application/json"), j.toString()))
                .build();
        Response resp = null;
        boolean rv = false;
        try {
            resp = baseApp.executeRequest(req);
            if (resp.isSuccessful()) {
                rv = true;
                baseApp.consumeStringResponse(resp);
            }
        }
        catch (IOException e) {
            Log.e(TAG, "in cmd_ack", e);
        }
        return rv;
    }
}
