package com.axway.apigwgcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.JsonUtil;
import com.axway.apigwgcm.util.StringUtil;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * Created by su on 3/22/2016.
 */
public class GcmSvc extends GcmListenerService {

    public static final String TAG = GcmSvc.class.getSimpleName();

    public static final String BROADCAST_REFRESH = "refresh";

    public static final String BROADCAST_CATEGORY = "gcm.broadcast";
    public static final String[] BROADCAST_ACTIONS = { BROADCAST_REFRESH };

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Log.d(TAG, StringUtil.format("onMessageReceived: %s, %s", from, data));
        if (data == null)
            return;
        if (data.containsKey("event")) {
            processEvent(data);
            return;
        }
        if (data.containsKey("alert")) {
            processAlert(data);
            return;
        }
        if (data.containsKey("command")) {
            processCommand(data);
        }
    }

    protected void processEvent(final Bundle data) {
        int smIcon = R.mipmap.ic_stat_notify_gcm_event;
        JsonObject json = JsonUtil.getInstance().eventFrom(data);
        Uri uri = DbHelper.EventColumns.CONTENT_URI;
        String subj = data.getString("event");
        String msg = "Message id: " + data.getString("msg_id", "<none>");   //data.getString("message", "");
        String sender = data.getString("sender");
        Uri newUri = null;
        if (uri != null) {
            ContentValues values = new ContentValues();
            long now = System.currentTimeMillis();
            values.put(DbHelper.CommonColumns.FLAG, DbHelper.FLAG_NEW);
            values.put(DbHelper.CommonColumns.CREATE_DATE, now);
            values.put(DbHelper.CommonColumns.MODIFY_DATE, SystemClock.elapsedRealtime());
            values.put(DbHelper.MsgColumns.SENDER, sender);
            values.put(DbHelper.MsgColumns.SUBJECT, subj);
            values.put(DbHelper.MsgColumns.MESSAGE, json == null ? msg : json.toString());
            if (json != null && json.has("trigger_names")) {
                values.put(DbHelper.EventColumns.TRIGGER_NAMES, json.get("trigger_names").getAsString());
            }
            newUri = getContentResolver().insert(uri, values);
        }
        Intent actIntent = new Intent(Intent.ACTION_VIEW);
        actIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actIntent.setAction(Intent.ACTION_VIEW);
        actIntent.setDataAndType(newUri, DbHelper.getMimeType(newUri, true));
        actIntent.putExtras(data);
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

    protected void processAlert(final Bundle data) {
        int smIcon = R.mipmap.ic_stat_notify_gcm_alert;
        JsonObject json = null;
        Uri uri = DbHelper.AlertColumns.CONTENT_URI;
        String subj = data.getString("alert");
        String msg = data.getString("message", "");
        String sender = data.getString("sender");
        Uri newUri = null;
        if (uri != null) {
            ContentValues values = new ContentValues();
            long now = System.currentTimeMillis();
            values.put(DbHelper.CommonColumns.FLAG, DbHelper.FLAG_NEW);
            values.put(DbHelper.CommonColumns.CREATE_DATE, now);
            values.put(DbHelper.CommonColumns.MODIFY_DATE, SystemClock.elapsedRealtime());
            values.put(DbHelper.MsgColumns.SENDER, sender);
            values.put(DbHelper.MsgColumns.SUBJECT, subj);
            values.put(DbHelper.MsgColumns.MESSAGE, msg);
            newUri = getContentResolver().insert(uri, values);
        }
        Intent actIntent = new Intent(Intent.ACTION_VIEW);
        actIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actIntent.setAction(Intent.ACTION_VIEW);
        actIntent.setDataAndType(newUri, DbHelper.getMimeType(newUri, true));
        actIntent.putExtras(data);
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

    protected void processCommand(final Bundle data) {
        int smIcon = R.mipmap.ic_stat_notify_gcm_cmd;
        JsonObject json = null;
        Uri uri = DbHelper.CommandColumns.CONTENT_URI;
        String cmd = data.getString("command");
        String params = data.getString("params", "");
        String ackUrl= data.getString("ack_url", "");
        if (ackUrl.startsWith("["))
            ackUrl = "";
        String sender = data.getString("sender");
        Uri newUri = null;
        if (uri != null) {
            ContentValues values = new ContentValues();
            long now = System.currentTimeMillis();
            values.put(DbHelper.CommonColumns.FLAG, DbHelper.FLAG_NEW);
            values.put(DbHelper.CommonColumns.CREATE_DATE, now);
            values.put(DbHelper.CommonColumns.MODIFY_DATE, SystemClock.elapsedRealtime());
            values.put(DbHelper.CommandColumns.SENDER, sender);
            values.put(DbHelper.CommandColumns.SUBJECT, cmd);
            values.put(DbHelper.CommandColumns.MESSAGE, params);
            values.put(DbHelper.CommandColumns.ACK_URL, ackUrl);
            newUri = getContentResolver().insert(uri, values);
        }
        if (newUri == null)
            return;
        Intent actIntent = new Intent(this, GcmCommandService.class);
//        actIntent.putExtra(DbHelper.CommandColumns.SUBJECT, cmd);
//        actIntent.putExtra(DbHelper.CommandColumns.MESSAGE, params);
//        actIntent.putExtra(DbHelper.CommandColumns.ACK_URL, ackUrl);
        actIntent.setData(newUri);
        startService(actIntent);
/*
        Intent actIntent = new Intent(Intent.ACTION_VIEW);
        actIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actIntent.setAction(Intent.ACTION_VIEW);
        actIntent.setDataAndType(newUri, DbHelper.getMimeType(newUri, true));
        actIntent.putExtras(data);
        PendingIntent pi = PendingIntent.getActivity(this, 0, actIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification n = new Notification.Builder(this)
                .setContentIntent(pi)
                .setSmallIcon(smIcon)
                .setStyle(new Notification.BigTextStyle().bigText(cmd))
                .setContentTitle(cmd)
                .setContentText(params)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int m = DbHelper.matchUri(uri);
        mgr.notify(m, n);
*/
        Intent broadcast = new Intent();
        broadcast.addCategory(BROADCAST_CATEGORY);
        broadcast.setAction(BROADCAST_REFRESH);
        broadcast.setData(uri);
        Log.d(TAG, "sending broadcast (refresh)");
        sendBroadcast(broadcast);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG, "onDeletedMessages");
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.d(TAG, StringUtil.format("onMessageSent: %s", msgId));
    }

    @Override
    public void onSendError(String msgId, String error) {
        super.onSendError(msgId, error);
        Log.d(TAG, StringUtil.format("onSendError: %s, %s", msgId, error));
    }
}
