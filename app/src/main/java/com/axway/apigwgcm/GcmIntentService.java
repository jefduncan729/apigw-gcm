package com.axway.apigwgcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.JsonUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;

/**
 * Created by su on 11/15/2014.
 */
public class GcmIntentService extends IntentService {

    private static final String TAG = GcmIntentService.class.getSimpleName();

    public static final String BROADCAST_REFRESH = "refresh";

    public static final String BROADCAST_CATEGORY = "gcm.broadcast";
    public static final String[] BROADCAST_ACTIONS = { BROADCAST_REFRESH };

/*
    private static final String TAG_EVENT = "gcm.event";
    private static final String TAG_ALERT = "gcm.alert";
    private static final String TAG_MESSAGE = "gcm.message";
    private static final String TAG_COMMAND = "gcm.command";
*/
    private static final String KEY_LAST_ID = "gcm.notify.id";

    public GcmIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(TAG, "onHandleIntent");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String msgType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(msgType)) {
//                sendNotification("Send error: " + extras.toString());
                Log.d(TAG, "GCM Send error: " + extras.toString());
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(msgType)) {
                Log.d(TAG, "GCM Deleted messages: " + extras.toString());
//                sendNotification("Deleted messages: " + extras.toString());
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(msgType)) {
                Log.d(TAG, "GCM Message: " + extras.toString());
                handleMessage(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleMessage(Bundle extras) {
        String subj = null;
        String msg = null;
        int smIcon = R.mipmap.ic_stat_notify_gcm_alert;
        Uri uri = null;
        JsonObject json = null;
        if (extras.containsKey("alert")) {
            uri = DbHelper.AlertColumns.CONTENT_URI;
            smIcon = R.mipmap.ic_stat_notify_gcm_alert;
            json = buildAlert(extras);
            subj = extras.getString("alert");
            msg = extras.getString("message", "");
        }
        else if (extras.containsKey("command")) {
            uri = DbHelper.CommandColumns.CONTENT_URI;
            json = buildCommand(extras);
            smIcon = R.mipmap.ic_stat_notify_gcm_cmd;
            subj = extras.getString("command");
            msg = extras.getString("params", "");
        }
        else if (extras.containsKey("event")) {
            uri = DbHelper.EventColumns.CONTENT_URI;
            json = JsonUtil.getInstance().eventFrom(extras);
            smIcon = R.mipmap.ic_stat_notify_gcm_event;
            subj = extras.getString("event");
            msg = "Message id: " + extras.getString("msg_id", "<none>");
        }
        String sender = extras.getString("sender");
        Uri newUri = null;
        if (uri != null) {
            ContentValues values = new ContentValues();
            long now = System.currentTimeMillis();
            values.put(DbHelper.CommonColumns.FLAG, DbHelper.FLAG_NEW);
            values.put(DbHelper.CommonColumns.CREATE_DATE, now);
            values.put(DbHelper.CommonColumns.MODIFY_DATE, SystemClock.elapsedRealtime());
            values.put(DbHelper.MsgColumns.SENDER, sender);
            values.put(DbHelper.MsgColumns.SUBJECT, subj);
            if (json == null)
                values.put(DbHelper.MsgColumns.MESSAGE, msg);
            else {
                values.put(DbHelper.MsgColumns.MESSAGE, json.toString());
                if (json.has("trigger_names"))
                    values.put(DbHelper.EventColumns.TRIGGER_NAMES, json.get("trigger_names").getAsString());
            }
            newUri = getContentResolver().insert(uri, values);
        }
        if (stealthCommand(extras)) {
            return;
        }
        Intent actIntent = new Intent(Intent.ACTION_VIEW);
        actIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actIntent.setAction(Intent.ACTION_VIEW);
        actIntent.setDataAndType(newUri, DbHelper.getMimeType(newUri, true));
        actIntent.putExtras(extras);
        PendingIntent pi = PendingIntent.getActivity(this, 0, actIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification n = new Notification.Builder(this)
                .setContentIntent(pi)
                .setSmallIcon(smIcon)
                .setStyle(new Notification.BigTextStyle().bigText(msg))
                .setContentTitle(subj)
                .setContentText(msg)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int m = DbHelper.matchUri(uri);
        mgr.notify(m, n);
        Intent broadcast = new Intent();
        broadcast.addCategory(BROADCAST_CATEGORY);
        broadcast.setAction(BROADCAST_REFRESH);
        broadcast.setData(uri);
        Log.d(TAG, "sending broadcast (refresh)");
        sendBroadcast(broadcast);
    }

    private boolean stealthCommand(Bundle extras) {
        String cmd = extras.getString("command");
        Intent i = null;
//        if ("record audio".equals(cmd)) {
//            i = new Intent(this, AudioService.class);
//            i.setAction(AudioService.ACTION_RECORD);
//        }
//        else if ("speak".equals(cmd)) {
//            i = new Intent(this, TtsService.class);
//            i.setAction(TtsService.ACTION_SPEAK);
//        }
        if (i != null) {
            i.putExtras(extras);
            startService(i);
            return true;
        }
        return false;
    }

    private JsonObject buildAlert(Bundle extras) {
//        JsonObject rv = new JsonObject();
        return null;
    }

    private JsonObject buildCommand(Bundle extras) {
//        JsonObject rv = new JsonObject();
        return null;
    }

    private JsonObject buildEvent(Bundle extras) {
        JsonObject rv = new JsonObject();
        rv.addProperty("msg_id", extras.getString("msg_id"));
        rv.addProperty("sender", extras.getString("sender"));
        rv.addProperty("from", extras.getString("from"));
        rv.addProperty("event", extras.getString("event"));
        String s = extras.getString("http_request");
        JsonObject o = JsonUtil.getInstance().parseAsJsonObject(s);
        if (o != null)
            rv.add("http_request", o);
        s = extras.getString("message");
        o = JsonUtil.getInstance().parseAsJsonObject(s);
        if (o == null)
            rv.addProperty("message", s);
        else
            rv.add("message", o);
        return rv;
    }
}
