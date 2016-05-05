package com.axway.apigwgcm.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.util.StringUtil;

import java.util.Locale;


/**
 * Created by su on 12/5/2014.
 */
public class DemoProvider extends ContentProvider {

    private static final String TAG = DemoProvider.class.getSimpleName();

    private DbHelper dbHelper;

    public static UriMatcher URI_MATCHER = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher rv = new UriMatcher(UriMatcher.NO_MATCH);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.ALERTS, DbHelper.ALERTS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.ALERTS + "/*", DbHelper.ALERT_ID);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.COMMANDS, DbHelper.COMMANDS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.COMMANDS + "/*", DbHelper.COMMAND_ID);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.EVENTS, DbHelper.EVENTS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.EVENTS + "/*", DbHelper.EVENT_ID);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.TRIGGERS, DbHelper.TRIGGERS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, DbHelper.Tables.TRIGGERS + "/*", DbHelper.TRIGGER_ID);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, "recent/" + DbHelper.Tables.ALERTS, DbHelper.RECENT_ALERTS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, "recent/" + DbHelper.Tables.COMMANDS, DbHelper.RECENT_COMMANDS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, "recent/" + DbHelper.Tables.EVENTS, DbHelper.RECENT_EVENTS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, "count/" + DbHelper.Tables.ALERTS, DbHelper.COUNT_ALERTS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, "count/" + DbHelper.Tables.COMMANDS, DbHelper.COUNT_COMMANDS);
        rv.addURI(DbHelper.CONTENT_AUTHORITY, "count/" + DbHelper.Tables.EVENTS, DbHelper.COUNT_EVENTS);
        return rv;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;    //(dbHelper != null);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder bldr = new SQLiteQueryBuilder();
        bldr.setProjectionMap(null);
//        String sql = null;
        String id = null;
        int limit = 0;
        boolean count = false;
        switch (URI_MATCHER.match(uri)) {
            case DbHelper.ALERTS:
                projection = DbHelper.AlertColumns.DEF_PROJECTION;
                bldr.setTables(DbHelper.Tables.ALERTS);
                break;
            case DbHelper.ALERT_ID:
                projection = DbHelper.AlertColumns.DEF_PROJECTION;
                bldr.setTables(DbHelper.Tables.ALERTS);
                id = uri.getPathSegments().get(1);
                break;
            case DbHelper.COMMANDS:
                projection = DbHelper.CommandColumns.DEF_PROJECTION;
                bldr.setTables(DbHelper.Tables.COMMANDS);
                break;
            case DbHelper.COMMAND_ID:
                projection = DbHelper.CommandColumns.DEF_PROJECTION;
                bldr.setTables(DbHelper.Tables.COMMANDS);
                id = uri.getPathSegments().get(1);
                break;
            case DbHelper.EVENTS:
                bldr.setTables(DbHelper.Tables.EVENTS);
                projection = DbHelper.EventColumns.DEF_PROJECTION;
                break;
            case DbHelper.EVENT_ID:
                bldr.setTables(DbHelper.Tables.EVENTS);
                projection = DbHelper.EventColumns.DEF_PROJECTION;
                id = uri.getPathSegments().get(1);
                break;
            case DbHelper.TRIGGERS:
                bldr.setTables(DbHelper.Tables.TRIGGERS);
                break;
            case DbHelper.TRIGGER_ID:
                bldr.setTables(DbHelper.Tables.TRIGGERS);
                id = uri.getPathSegments().get(1);
                break;
            case DbHelper.RECENT_ALERTS:
                bldr.setTables(DbHelper.Tables.ALERTS);
                limit = getNumRecents();
                break;
            case DbHelper.RECENT_COMMANDS:
                bldr.setTables(DbHelper.Tables.COMMANDS);
                limit = getNumRecents();
                break;
            case DbHelper.RECENT_EVENTS:
                bldr.setTables(DbHelper.Tables.EVENTS);
                projection = DbHelper.EventColumns.DEF_PROJECTION;
                limit = getNumRecents();
                break;
            case DbHelper.COUNT_ALERTS:
                bldr.setTables(DbHelper.Tables.ALERTS);
                count = true;
                break;
            case DbHelper.COUNT_COMMANDS:
                bldr.setTables(DbHelper.Tables.COMMANDS);
                count = true;
                break;
            case DbHelper.COUNT_EVENTS:
                bldr.setTables(DbHelper.Tables.EVENTS);
                count = true;
                break;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (TextUtils.isEmpty(bldr.getTables()))
            return null;
        String sql = null;
        if (count) {
            projection = new String[] { "count(*)" };
        }
        else {
            if (id != null)
                bldr.appendWhere(DbHelper.CommonColumns._ID + "=" + id);
            if (TextUtils.isEmpty(sortOrder))
                sortOrder = DbHelper.CommonColumns.DEF_SORT_ORDER;
            if (projection == null)
                projection = DbHelper.MsgColumns.DEF_PROJECTION;
        }
        String limitStr = null;
        if (limit != 0) {
            sortOrder = DbHelper.CommonColumns.CREATE_DATE + " DESC";
            limitStr = Integer.toString(limit);
            sql = bldr.buildQuery(projection, selection, null, null, sortOrder, limitStr);
        }
        Cursor rv = null;
        if (sql == null) {
            Log.v(TAG, StringUtil.format("executing builder query: %s for uri %s", selection, uri));
            rv = bldr.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        }
        else {
            Log.v(TAG, StringUtil.format("executing raw SQL: %s for uri %s", sql, uri));
            rv = db.rawQuery(sql, selectionArgs);
        }
        Log.v(TAG, "row count: " + (rv == null ? "null" : Integer.toString(rv.getCount())));
        return rv;
    }

    private int getNumRecents() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getInt(Constants.KEY_NUM_RECENTS, Constants.DEF_NUM_RECENTS);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        String rv = null;
        switch (URI_MATCHER.match(uri)) {
            case DbHelper.ALERTS:
                rv = DbHelper.AlertColumns.CONTENT_TYPE;
                break;
            case DbHelper.ALERT_ID:
                rv = DbHelper.AlertColumns.CONTENT_ITEM_TYPE;
                break;
            case DbHelper.COMMANDS:
                rv = DbHelper.CommandColumns.CONTENT_TYPE;
                break;
            case DbHelper.COMMAND_ID:
                rv = DbHelper.CommandColumns.CONTENT_ITEM_TYPE;
                break;
            case DbHelper.EVENTS:
                rv = DbHelper.EventColumns.CONTENT_TYPE;
                break;
            case DbHelper.EVENT_ID:
                rv = DbHelper.EventColumns.CONTENT_ITEM_TYPE;
                break;
            case DbHelper.TRIGGERS:
                rv = DbHelper.TriggerColumns.CONTENT_TYPE;
                break;
            case DbHelper.TRIGGER_ID:
                rv = DbHelper.TriggerColumns.CONTENT_ITEM_TYPE;
                break;
        }
        return rv;
    }

    private void cleanCommonValues(ContentValues values) {
        long now = System.currentTimeMillis();
        if (!values.containsKey(DbHelper.CommonColumns.MODIFY_DATE))
            values.put(DbHelper.CommonColumns.MODIFY_DATE, SystemClock.elapsedRealtime());
        if (!values.containsKey(DbHelper.CommonColumns.CREATE_DATE))
            values.put(DbHelper.CommonColumns.CREATE_DATE, now);
        if (!values.containsKey(DbHelper.CommonColumns.STATUS))
            values.put(DbHelper.CommonColumns.STATUS, 0);
        if (!values.containsKey(DbHelper.CommonColumns.FLAG))
            values.put(DbHelper.CommonColumns.FLAG, 0);
    }

    private void cleanMsgValues(ContentValues values) {
        if (!values.containsKey(DbHelper.MsgColumns.SUBJECT))
            values.put(DbHelper.MsgColumns.SUBJECT, "");
        if (!values.containsKey(DbHelper.MsgColumns.MESSAGE))
            values.put(DbHelper.MsgColumns.MESSAGE, "");
    }

    private void cleanAlertValues(ContentValues values) {
        cleanCommonValues(values);
        cleanMsgValues(values);
        if (!values.containsKey(DbHelper.AlertColumns.DETAILS))
            values.put(DbHelper.AlertColumns.DETAILS, "");
    }

    private void cleanCommandValues(ContentValues values) {
        cleanCommonValues(values);
        cleanMsgValues(values);
        if (!values.containsKey(DbHelper.CommandColumns.ACK_URL))
            values.put(DbHelper.CommandColumns.ACK_URL, "");
    }

    private void cleanEventValues(ContentValues values) {
        cleanCommonValues(values);
        cleanMsgValues(values);
        if (!values.containsKey(DbHelper.EventColumns.TRIGGER_NAMES))
            values.put(DbHelper.EventColumns.TRIGGER_NAMES, "");
    }

    private void cleanTriggerValues(ContentValues values) {
        cleanCommonValues(values);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues valuesIn) {
        if (valuesIn == null)
            return null;
        ContentValues values = new ContentValues(valuesIn);
        Log.d(TAG, StringUtil.format("insert: raw values: %s", values.toString()));
        boolean fromSync = false;
        if (values.containsKey(Constants.EXTRA_FROM_SYNC)) {
            fromSync = true;
            values.remove(Constants.EXTRA_FROM_SYNC);
        }
        String tblName = null;
        Uri baseUri = null;
        switch (URI_MATCHER.match(uri)) {
            case DbHelper.ALERTS:
                cleanAlertValues(values);
                baseUri = DbHelper.AlertColumns.CONTENT_URI;
                tblName = DbHelper.Tables.ALERTS;
                break;
            case DbHelper.COMMANDS:
                cleanCommandValues(values);
                baseUri = DbHelper.CommandColumns.CONTENT_URI;
                tblName = DbHelper.Tables.COMMANDS;
                break;
            case DbHelper.EVENTS:
                cleanEventValues(values);
                baseUri = DbHelper.EventColumns.CONTENT_URI;
                tblName = DbHelper.Tables.EVENTS;
                break;
            case DbHelper.TRIGGERS:
                if (!fromSync)
                    cleanTriggerValues(values);
                baseUri = DbHelper.TriggerColumns.CONTENT_URI;
                tblName = DbHelper.Tables.TRIGGERS;
                break;
        }
        if (baseUri == null || TextUtils.isEmpty(tblName)) {
            Log.d(TAG, "bad insert attempt");
            return null;
        }
        if (values.containsKey(DbHelper.CommonColumns._ID) && !fromSync) {
            //remove IDs for inserts, all are auto-increment
            values.remove(DbHelper.CommonColumns._ID);
        }
        long remoteId = -1;
        if (fromSync) {
            remoteId = values.getAsLong(DbHelper.CommonColumns._ID);
        }
        else {
            values.put(DbHelper.CommonColumns.FLAG, DbHelper.FLAG_NEW);
        }
        Log.d(TAG, StringUtil.format("insert: cleaned values: %s", values.toString()));
        Uri rv = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(tblName, null, values);
        if (rowId > 0) {
            rv = ContentUris.withAppendedId(baseUri, rowId);
            if (fromSync) {
            }
            else {
                notifyChg(baseUri, null, true);
            }
        }
        Log.v(TAG, "returning " + (rv == null ? "null" : rv.toString()));
        return rv;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String tblName = null;
        String s = uri.getLastPathSegment();
        boolean fromSync = uri.getBooleanQueryParameter(Constants.EXTRA_FROM_SYNC, false);
        long id = 0;
        try {
            id = Long.parseLong(s);
        }
        catch (NumberFormatException e) {
            id = 0;
        }
        switch (URI_MATCHER.match(uri)) {
            case DbHelper.ALERTS:
                break;
            case DbHelper.ALERT_ID:
                tblName = DbHelper.Tables.ALERTS;
                break;
            case DbHelper.COMMANDS:
                break;
            case DbHelper.COMMAND_ID:
                tblName = DbHelper.Tables.COMMANDS;
                break;
            case DbHelper.EVENTS:
                break;
            case DbHelper.EVENT_ID:
                tblName = DbHelper.Tables.EVENTS;
                break;
            case DbHelper.TRIGGERS:
                break;
            case DbHelper.TRIGGER_ID:
                tblName = DbHelper.Tables.TRIGGERS;
                break;
        }
        if (TextUtils.isEmpty(tblName) || id == 0) {
            //don't allow mass-deletions (easy to change if desired)
            Log.d(TAG, "bad delete attempt");
            return 0;
        }
        if (selection == null)
            selection = DbHelper.CommonColumns._ID + " = ?";
        if (selectionArgs == null)
            selectionArgs = new String[] { Long.toString(id) };
        Log.d(TAG, "delete from " + tblName + " where id=" + Long.toString(id));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rv = 0;
        if (fromSync) {
            rv = db.delete(tblName, selection, selectionArgs);
            Log.d(TAG, "deleted: " + Integer.toString(rv));
        }
        else {
            ContentValues values = new ContentValues();
            values.put(DbHelper.TriggerColumns.FLAG, DbHelper.FLAG_DELETED);

            rv = db.update(tblName, values, selection, selectionArgs);
            Log.d(TAG, "set deleted flag: " + Integer.toString(rv));
        }
        if (rv > 0 && !fromSync) {
            notifyChg(uri, null, true);
        }
        return rv;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues valuesIn, String selection, String[] selectionArgs) {
        String id = null;
        try {
            id = uri.getPathSegments().get(1);
        }
        catch (IndexOutOfBoundsException e) {
            id = null;
        }
        if (TextUtils.isEmpty(id) && TextUtils.isEmpty(selection)) {
            Log.d(TAG, "update with no id or where clause");
            return 0;
        }
        ContentValues values = (valuesIn == null ? new ContentValues() : new ContentValues(valuesIn));
        Log.d(TAG, StringUtil.format("update: raw values: %s", values.toString()));
        boolean fromSync = false;
        if (values.containsKey(Constants.EXTRA_FROM_SYNC)) {
            fromSync = true;
            values.remove(Constants.EXTRA_FROM_SYNC);
        }
//        if (!values.containsKey(DbHelper.CommonColumns.MODIFY_DATE))
//            values.put(DbHelper.CommonColumns.MODIFY_DATE, System.currentTimeMillis());
        String tblNm = null;
        switch (URI_MATCHER.match(uri)) {
            case DbHelper.ALERTS:
            case DbHelper.ALERT_ID:
                cleanAlertValues(values);
                tblNm = DbHelper.Tables.ALERTS;
                break;
            case DbHelper.COMMANDS:
            case DbHelper.COMMAND_ID:
                cleanCommandValues(values);
                tblNm = DbHelper.Tables.COMMANDS;
                break;
            case DbHelper.EVENTS:
            case DbHelper.EVENT_ID:
                cleanEventValues(values);
                tblNm = DbHelper.Tables.EVENTS;
                break;
            case DbHelper.TRIGGERS:
            case DbHelper.TRIGGER_ID:
                if (!fromSync)
                    cleanTriggerValues(values);
                tblNm = DbHelper.Tables.TRIGGERS;
                break;
        }
        if (TextUtils.isEmpty(tblNm)) {
            Log.d(TAG, "bad update attempt");
            return 0;
        }
        if (TextUtils.isEmpty(selection)) {
            selection = DbHelper.CommonColumns._ID + " = " + id;
        }
        if (!selection.contains(DbHelper.CommonColumns._ID))
            selection = (DbHelper.CommonColumns._ID + " = " + id) + selection;
        if (!fromSync)
            values.put(DbHelper.CommonColumns.FLAG, DbHelper.FLAG_UPDATED);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(TAG, StringUtil.format("update: %s, cleaned values: %s", uri, values));
        int rv = db.update(tblNm, values, selection, selectionArgs);
        Log.v(TAG, StringUtil.format("updated: %d", rv));
        if (rv > 0 && !fromSync) {
            notifyChg(uri, null, true);
        }
        return rv;
    }

    private void notifyChg(Uri uri) {
        notifyChg(uri, null);
    }

    private void notifyChg(Uri uri, ContentObserver obs) {
        notifyChg(uri, obs, false);
    }

    private void notifyChg(Uri uri, ContentObserver obs, boolean sync) {
        if (uri == null)
            return;
        Context ctx = getContext();
        if (ctx == null)
            return;
        ContentResolver cr = ctx.getContentResolver();
        if (cr == null)
            return;
        Log.d(TAG, String.format("calling notifyChange: %s, syncToNetwork: %s", uri, sync));
        Uri b = DbHelper.getBaseUri(uri);
        if (b != null)
            cr.notifyChange(b, obs, sync);
    }
}
