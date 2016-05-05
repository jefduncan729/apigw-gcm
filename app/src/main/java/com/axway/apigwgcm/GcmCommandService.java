package com.axway.apigwgcm;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.StringUtil;

/**
 * Created by su on 5/5/2016.
 */
public class GcmCommandService extends IntentService {
    private static final String TAG = GcmCommandService.class.getSimpleName();

    public GcmCommandService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
        if (c.moveToFirst()) {
            String cmd = c.getString(DbHelper.CommandColumns.NDX_SUBJECT);
            String p = c.getString(DbHelper.CommandColumns.NDX_MESSAGE);
            String ackUrl = c.getString(DbHelper.CommandColumns.NDX_ACK_URL);
            Log.d(TAG, StringUtil.format("cmd: %s, params: %s, ack: %s", cmd, p, ackUrl));
        }
        c.close();
    }
}
