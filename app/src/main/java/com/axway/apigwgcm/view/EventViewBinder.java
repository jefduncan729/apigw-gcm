package com.axway.apigwgcm.view;

import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.DateUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.google.gson.JsonObject;

/**
 * Created by su on 12/8/2014.
 */
public class EventViewBinder extends CursorViewBinder {

    public EventViewBinder() {
        super();
    }

    private JsonObject event;

    private JsonObject getEvent(String s) {
        if (event == null) {
            try {
                event = JsonUtil.getInstance().parseAsJsonObject(s);
            }
            catch (Exception e) {
                event = null;
            }
        }
        return event;
    }

    @Override
    public void bindListView(final View view, final Cursor cursor) {
        BasicViewHolder holder = (BasicViewHolder)view.getTag();
        if (holder == null)
            return;
        String s = cursor.getString(DbHelper.EventColumns.NDX_MESSAGE);
        event = getEvent(s);
        StringBuilder sb = new StringBuilder();
        if (event == null)
            sb.append("no details");
        else {
            if (event.has("msg_id")) {
                sb.append(event.get("msg_id").getAsString());
            }
            if (sb.length() > 0)
                sb.append("\n");
            long d = cursor.getLong(DbHelper.CommonColumns.NDX_CREATE_DATE);
            if (d > 0) {
                sb.append("\nReceived: ").append(DateUtil.relativeTime(d));
                sb.append("\n").append(DateUtil.formatDatetime(d));
            }
        }
        holder.setText1(buildSummary(cursor));
        holder.getTextView1().setTag(cursor.getLong(DbHelper.CommonColumns.NDX_ID));
        holder.setText2(sb.toString());
        holder.setImageResource(getIconId());
    }

    @Override
    public void bindDetailView(final View view, final Cursor cursor) {
        if (cursor == null)
            return;
        if (cursor.isBeforeFirst())
            cursor.moveToFirst();
        BasicViewHolder holder = (BasicViewHolder)view.getTag();
        if (holder == null) {
            holder = new BasicViewHolder(view);
            view.setTag(holder);
        }
        String s = cursor.getString(DbHelper.EventColumns.NDX_MESSAGE);
        event = getEvent(s);
        StringBuilder sb = new StringBuilder();
        if (event == null) {
            if (TextUtils.isEmpty(s))
                sb.append("no details");
            else
                sb.append(s);
        }
        else {
            if (event.has("msg_id")) {
                sb.append(event.get("msg_id").getAsString());
            }
            if (event.has("sender")) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append("Sender: ").append(event.get("sender").getAsString());
            }
            if (event.has("http_request")) {
                JsonObject h = event.getAsJsonObject("http_request");
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append("HTTP\n    protocol: ").append(h.get("protocol").getAsString());
                sb.append("\n    verb: ").append(h.get("verb").getAsString());
                sb.append("\n    path: ").append(h.get("path").getAsString());
                sb.append("\n    version: ").append(h.get("version").getAsString());
            }
            if (event.has("from") && !event.get("from").isJsonNull()) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append("From: ").append(event.get("from").getAsString());
            }
            if (event.has("message") && !event.get("message").isJsonNull()) {
                if (sb.length() > 0)
                    sb.append("\n");
                if (event.get("message").isJsonObject()) {
                    JsonObject j = event.getAsJsonObject("message");
                    sb.append("Message\n    source: ").append(j.get("source").getAsString());
                    sb.append("\n    client_name: ").append(j.get("client_name").getAsString());
                }
                else {
                    sb.append("Message: ").append(event.get("message").getAsString());
                }
            }
/*
            if (event.has("trigger_names")) {
                JsonObject t = event.getAsJsonObject("trigger");
                if (sb.length() > 0)
                    sb.append("\n");
                if (t.has("id"))
                    sb.append("Trigger\n    id: ").append(t.get("id").getAsString());
                if (t.has("priority"))
                    sb.append("\n    priority: ").append(t.get("priority").getAsString());
                if (t.has("name"))
                    sb.append("\n    name: ").append(t.get("name").getAsString());
                if (t.has("expression"))
                    sb.append("\n    expr: ").append(t.get("expression").getAsString());
            }
*/
        }
        if (sb.length() > 0)
            sb.append("\n");
        long d = cursor.getLong(DbHelper.CommonColumns.NDX_CREATE_DATE);
        if (d > 0) {
            sb.append("\nReceived: ").append(DateUtil.relativeTime(d)); //relativeTime(d));
            sb.append("\n").append(DateUtil.formatDatetime(d));
        }
        String tn = cursor.getString(DbHelper.EventColumns.NDX_TRIGGER_NAMES);
        if (!TextUtils.isEmpty(tn))
            sb.append("\nTrigger: ").append(tn);
        holder.setText1(buildSummary(cursor));
        holder.setText2(sb.toString());
        holder.setImageResource(getIconId());
        holder.showImageView(true);
    }
}
