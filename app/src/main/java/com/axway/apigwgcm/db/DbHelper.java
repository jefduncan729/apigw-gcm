package com.axway.apigwgcm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;

/**
 * Created by su on 12/5/2014.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();

    private static final int[] DRAWABLE_IDS = { R.mipmap.ic_nav_action_warning, R.mipmap.ic_nav_action_flash_on, R.mipmap.ic_nav_action_import_export, R.mipmap.ic_nav_action_attachment };

    public static final String CONTENT_AUTHORITY = Constants.ACCOUNT_TYPE;
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String CONTENT_TYPE = "vnd.axway.gcm.demo.";
    private static final String BASE_CONTENT_TYPE="vnd.android.cursor.dir/" + CONTENT_TYPE;
    private static final String BASE_CONTENT_ITEM_TYPE="vnd.android.cursor.item/" + CONTENT_TYPE;

    private static final int BASE = 0;
    public static final int NO_MATCH = BASE - 1;

    public static final int FLAG_INSYNC = 0;
    public static final int FLAG_NEW = 1;
    public static final int FLAG_UPDATED = 2;
    public static final int FLAG_DELETED = 3;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_ACKED = STATUS_ENABLED;

    public static final int ALERTS = BASE + 1;
    public static final int COMMANDS = BASE + 2;
    public static final int EVENTS = BASE + 3;
    public static final int TRIGGERS = BASE + 4;

    public static final int ALERT_ID = BASE + 5;
    public static final int COMMAND_ID = BASE + 6;
    public static final int EVENT_ID = BASE + 7;
    public static final int TRIGGER_ID = BASE + 8;

    public static final int RECENT_ALERTS = BASE + 101;
    public static final int RECENT_COMMANDS = BASE + 102;
    public static final int RECENT_EVENTS = BASE + 103;

    public static final int COUNT_ALERTS = BASE + 201;
    public static final int COUNT_COMMANDS = BASE + 202;
    public static final int COUNT_EVENTS = BASE + 203;

    public static final String DB_NAME = "gcm_demo.db";
    public static final int VERSION_1 = 1;
    public static final int VERSION_2 = 2;

    public interface Tables {
        public static final String ALERTS = "alerts";
        public static final String COMMANDS = "commands";
        public static final String EVENTS = "events";
        public static final String TRIGGERS = "triggers";
    }

    public interface CommonColumns extends BaseColumns {
        public static final String STATUS = "status";
        public static final String FLAG = "flag";
        public static final String CREATE_DATE = "created";
        public static final String MODIFY_DATE = "modified";

        public static final int NDX_ID = 			0;
        public static final int NDX_STATUS = 		NDX_ID + 1;
        public static final int NDX_FLAG = 			NDX_ID + 2;
        public static final int NDX_CREATE_DATE = 	NDX_ID + 3;
        public static final int NDX_MODIFY_DATE = 	NDX_ID + 4;

//        public static final String[] DEF_PROJECTION = { _ID, STATUS, FLAG, CREATE_DATE, MODIFY_DATE };
        public static final String DEF_SORT_ORDER = _ID + " DESC";
    }

    public interface MsgColumns extends CommonColumns {
        public static final String SUBJECT = "subject";
        public static final String MESSAGE = "message";
        public static final String SENDER = "sender";

        public static final int NDX_SUBJECT = 	    NDX_MODIFY_DATE+1;
        public static final int NDX_MESSAGE = 	    NDX_MODIFY_DATE+2;
        public static final int NDX_SENDER = 	    NDX_MODIFY_DATE+3;

        public static final String[] DEF_PROJECTION = { _ID, STATUS, FLAG, CREATE_DATE, MODIFY_DATE, SUBJECT, MESSAGE, SENDER };
    }

    public interface AlertColumns extends MsgColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.ALERTS).build();
        public static final String CONTENT_TYPE = BASE_CONTENT_TYPE + Tables.ALERTS;
        public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE + Tables.ALERTS;

        public static final String DETAILS = "details";

        public static final int NDX_DETAILS =       NDX_SENDER+1;

        public static final String[] DEF_PROJECTION = { _ID, STATUS, FLAG, CREATE_DATE, MODIFY_DATE, SUBJECT, MESSAGE, SENDER, DETAILS };

    }

    public interface CommandColumns extends MsgColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.COMMANDS).build();
        public static final String CONTENT_TYPE = BASE_CONTENT_TYPE + Tables.COMMANDS;
        public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE + Tables.COMMANDS;

        public static final String ACK_URL = "ack_url";

        public static final int NDX_ACK_URL = NDX_SENDER + 1;
        public static final String[] DEF_PROJECTION = { _ID, STATUS, FLAG, CREATE_DATE, MODIFY_DATE, SUBJECT, MESSAGE, SENDER, ACK_URL };
    }

    public interface EventColumns extends MsgColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.EVENTS).build();
        public static final String CONTENT_TYPE = BASE_CONTENT_TYPE + Tables.EVENTS;
        public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE + Tables.EVENTS;

        public static final String DETAILS = "details";
        public static final String TRIGGER_NAMES = "trigger_names";

        public static final int NDX_DETAILS = NDX_SENDER + 1;
        public static final int NDX_TRIGGER_NAMES = NDX_SENDER + 2;

        public static final String[] DEF_PROJECTION = { _ID, STATUS, FLAG, CREATE_DATE, MODIFY_DATE, SUBJECT, MESSAGE, SENDER, DETAILS, TRIGGER_NAMES };
    }

    public interface TriggerColumns extends CommonColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.TRIGGERS).build();
        public static final String CONTENT_TYPE = BASE_CONTENT_TYPE + Tables.TRIGGERS;
        public static final String CONTENT_ITEM_TYPE = BASE_CONTENT_ITEM_TYPE + Tables.TRIGGERS;

        public static final String TYPE = "_type";
        public static final String NAME = "name";
        public static final String EXPRESSION = "expr";
        public static final String PRIORITY = "priority";

        public static final int NDX_TYPE = NDX_MODIFY_DATE + 1;
        public static final int NDX_NAME = NDX_MODIFY_DATE + 2;
        public static final int NDX_EXPR = NDX_MODIFY_DATE + 3;
        public static final int NDX_PRIORITY = NDX_MODIFY_DATE + 4;

        public static final String[] DEF_PROJECTION = { _ID, STATUS, FLAG, CREATE_DATE, MODIFY_DATE, TYPE, NAME, EXPRESSION, PRIORITY };
        public static final String DEF_SORT_ORDER = TYPE + " ASC, " + _ID + " ASC";
    }

    public static String DASHBOARD_MIMETYPES[] = { AlertColumns.CONTENT_TYPE, CommandColumns.CONTENT_TYPE, EventColumns.CONTENT_TYPE };

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION_1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "creating database " + db.getPath());
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeTables(db, oldVersion, newVersion);
    }

    public static String getMimeType(final Uri uri) {
        return getMimeType(DemoProvider.URI_MATCHER.match(uri), false);
    }

    public static String getMimeType(final Uri uri, final boolean itemType) {
        if (uri == null)
            return null;
        return getMimeType(DemoProvider.URI_MATCHER.match(uri), itemType);
    }

    public static String getMimeType(final int match, final boolean itemType) {
        String rv = null;
        switch (match) {
            case ALERT_ID:
            case ALERTS:
            case RECENT_ALERTS:
            case COUNT_ALERTS:
                rv = (itemType ? AlertColumns.CONTENT_ITEM_TYPE : AlertColumns.CONTENT_TYPE);
            break;
            case COMMAND_ID:
            case COMMANDS:
            case RECENT_COMMANDS:
            case COUNT_COMMANDS:
                rv = (itemType ? CommandColumns.CONTENT_ITEM_TYPE : CommandColumns.CONTENT_TYPE);
            break;
            case EVENT_ID:
            case EVENTS:
            case RECENT_EVENTS:
            case COUNT_EVENTS:
                rv = (itemType ? EventColumns.CONTENT_ITEM_TYPE : EventColumns.CONTENT_TYPE);
            break;
            case TRIGGER_ID:
            case TRIGGERS:
                rv = (itemType ? TriggerColumns.CONTENT_ITEM_TYPE : TriggerColumns.CONTENT_TYPE);
            break;
        }
        return rv;
    }

    public static int matchUri(final Uri uri) {
        if (uri == null)
            return NO_MATCH;
        return DemoProvider.URI_MATCHER.match(uri);
    }

    public static String getTitle(final Uri uri) {
        return getTitle(matchUri(uri));
    }

    public static String getTitle(final int dbType) {
        String base = null;
        boolean dtls = false;
        switch (dbType) {
            case ALERT_ID:
                dtls = true;
            case ALERTS:
            case RECENT_ALERTS:
            case COUNT_ALERTS:
                base = "Alert";
                break;
            case COMMAND_ID:
                dtls = true;
            case COMMANDS:
            case RECENT_COMMANDS:
            case COUNT_COMMANDS:
                base = "Command";
                break;
            case EVENT_ID:
                dtls = true;
            case EVENTS:
            case RECENT_EVENTS:
            case COUNT_EVENTS:
                base = "Event";
                break;
            case TRIGGER_ID:
                dtls = true;
            case TRIGGERS:
                base = "Event Trigger";
                break;
        }
        if (base == null)
            return null;
        if (dtls)
            return base + " Details";
        return base + "s";
    }

    public static Uri getRecentsUri(final int type) {
        switch (type) {
            case ALERTS:
                return BASE_CONTENT_URI.buildUpon().appendPath("recent").appendPath(Tables.ALERTS).build();
            case COMMANDS:
                return BASE_CONTENT_URI.buildUpon().appendPath("recent").appendPath(Tables.COMMANDS).build();
            case EVENTS:
                return BASE_CONTENT_URI.buildUpon().appendPath("recent").appendPath(Tables.EVENTS).build();
        }
        return null;
    }

    public static Uri getBaseUri(final Uri uri) {
        int type = matchUri(uri);
        switch (type) {
            case ALERTS:
            case ALERT_ID:
            case RECENT_ALERTS:
            case COUNT_ALERTS:
                return AlertColumns.CONTENT_URI;
            case COMMAND_ID:
            case RECENT_COMMANDS:
            case COUNT_COMMANDS:
            case COMMANDS:
                return CommandColumns.CONTENT_URI;
            case EVENT_ID:
            case RECENT_EVENTS:
            case COUNT_EVENTS:
            case EVENTS:
                return EventColumns.CONTENT_URI;
            case TRIGGERS:
            case TRIGGER_ID:
                return TriggerColumns.CONTENT_URI;
        }
        return null;
    }

    public static Uri getBaseUri(final int type) {
        switch (type) {
            case ALERTS:
                return AlertColumns.CONTENT_URI;
            case COMMANDS:
                return CommandColumns.CONTENT_URI;
            case EVENTS:
                return EventColumns.CONTENT_URI;
        }
        return null;
    }

    public static Uri getCountUri(final int type) {
        switch (type) {
            case ALERTS:
                return BASE_CONTENT_URI.buildUpon().appendPath("count").appendPath(Tables.ALERTS).build();
            case COMMANDS:
                return BASE_CONTENT_URI.buildUpon().appendPath("count").appendPath(Tables.COMMANDS).build();
            case EVENTS:
                return BASE_CONTENT_URI.buildUpon().appendPath("count").appendPath(Tables.EVENTS).build();
        }
        return null;
    }

    public static int getIconId(int type) {
        int id = -1;
        switch (type) {
            case ALERTS:
            case RECENT_ALERTS:
            case ALERT_ID:
                id = 0;
            break;
            case COMMANDS:
            case RECENT_COMMANDS:
            case COMMAND_ID:
                id = 1;
            break;
            case EVENT_ID:
            case RECENT_EVENTS:
            case EVENTS:
                id = 2;
            break;
            case TRIGGER_ID:
            case TRIGGERS:
                id = 3;
                break;
        }
        if (id >= 0 && id < DRAWABLE_IDS.length)
            return DRAWABLE_IDS[id];
        return 0;
    }

    private void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "upgrade tables from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
        upgradeAlerts(db, oldVersion, newVersion);
        upgradeCommands(db, oldVersion, newVersion);
        upgradeEvents(db, oldVersion, newVersion);
    }

    private void createTables(SQLiteDatabase db) {
        createAlerts(db);
        createCommands(db);
        createEvents(db);
        createTriggers(db);
    }

    private void addCommonColumns(StringBuilder sb) {
        sb.append(CommonColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(CommonColumns.STATUS).append(" INTEGER DEFAULT ").append(STATUS_ENABLED).append(", ")
                .append(CommonColumns.FLAG).append(" INTEGER DEFAULT ").append(FLAG_NEW).append(", ")
                .append(CommonColumns.CREATE_DATE).append(" LONG DEFAULT 0, ")
                .append(CommonColumns.MODIFY_DATE).append(" LONG DEFAULT 0, ");
    }

    private void addMsgColumns(StringBuilder sb) {
        sb.append(MsgColumns.SUBJECT).append(" VARCHAR(255) NULL DEFAULT NULL, ")
                .append(MsgColumns.MESSAGE).append(" TEXT NULL DEFAULT NULL, ")
                .append(MsgColumns.SENDER).append(" VARCHAR(128) NULL DEFAULT NULL, ");
    }

    private StringBuilder createStatement(String tblNm) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(tblNm).append("(");
        addCommonColumns(sb);
        return sb;
    }

    private void executeSql(SQLiteDatabase db, String sql) {
        Log.d(TAG, "executing SQL: " + sql);
        db.execSQL(sql);
    }

    private void createAlerts(SQLiteDatabase db) {
        StringBuilder sb = createStatement(Tables.ALERTS);
        addMsgColumns(sb);
        sb.append(AlertColumns.DETAILS).append(" VARCHAR(1024) NULL DEFAULT NULL").append(")");
        executeSql(db, sb.toString());
    }

    private void createCommands(SQLiteDatabase db) {
        StringBuilder sb = createStatement(Tables.COMMANDS);
        addMsgColumns(sb);
        sb.append(CommandColumns.ACK_URL).append(" VARCHAR(1024) NULL DEFAULT NULL").append(")");
        executeSql(db, sb.toString());
    }

    private void createEvents(SQLiteDatabase db) {
        StringBuilder sb = createStatement(Tables.EVENTS);
        addMsgColumns(sb);
        sb.append(EventColumns.DETAILS).append(" VARCHAR(1024) NULL DEFAULT NULL, ")
            .append(EventColumns.TRIGGER_NAMES).append(" VARCHAR(255) NULL DEFAULT NULL")
            .append(")");
        executeSql(db, sb.toString());
    }

    private void createTriggers(SQLiteDatabase db) {
        StringBuilder sb = createStatement(Tables.TRIGGERS);
        sb.append(TriggerColumns.TYPE).append(" INTEGER DEFAULT 0, ")
            .append(TriggerColumns.PRIORITY).append(" INTEGER DEFAULT 5, ")
            .append(TriggerColumns.NAME).append(" VARCHAR(255) NOT NULL, ")
            .append(TriggerColumns.EXPRESSION).append(" TEXT NULL DEFAULT NULL")
            .append(")");
        executeSql(db, sb.toString());
    }

    private void upgradeAlerts(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void upgradeCommands(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void upgradeEvents(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void ensureTablesExist() {
        SQLiteDatabase db = getWritableDatabase();
        createTables(db);
    }
}
