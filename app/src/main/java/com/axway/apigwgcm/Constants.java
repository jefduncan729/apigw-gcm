package com.axway.apigwgcm;

import android.content.DialogInterface;

/**
 * Created by su on 11/18/2014.
 */
public class Constants {
    public static final String ACCOUNT_TYPE = "com.axway.apigwgcm";

    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = HTTP_SCHEME + "s";

    public static final String EXTRA_BASE = "gcm.demo.";
    public static final String EXTRA_ACTION = EXTRA_BASE + "action";
    public static final String EXTRA_ITEM_ID = EXTRA_BASE + "item.id";
    public static final String EXTRA_ITEM_NAME = EXTRA_BASE + "item.name";
    public static final String EXTRA_ITEM_TYPE = EXTRA_BASE + "item.type";
    public static final String EXTRA_ACCESS_TOKEN = EXTRA_BASE + "access.token";
    public static final String EXTRA_GATEWAY_HOST = EXTRA_BASE + "gtw.host";
    public static final String EXTRA_GATEWAY_PORT = EXTRA_BASE + "gtw.port";
    public static final String EXTRA_NEW_ACCT = EXTRA_BASE + "new.acct";
    public static final String EXTRA_ROW_COUNT = EXTRA_BASE + "row.count";
    public static final String EXTRA_URL = EXTRA_BASE + "url";
    public static final String EXTRA_PARSE_RESPONSE = EXTRA_BASE + "parse.resp";
    public static final String EXTRA_LAST_SYNC_TIME = EXTRA_BASE + "last.sync.time";
    public static final String EXTRA_FROM_SYNC = EXTRA_BASE + "from.sync";

    public static final String AUTH_TOKEN_TYPE = ACCOUNT_TYPE + ".auth";

    public static final String GCM_PROJECT_ID = "659961194860"; //"404585072562";
    public static final String GCM_API_KEY = "AIzaSyDk2N-aVBDxJnPJ4NxlP6zKxjW8zky23MM"; //"AIzaSyBBrkVTbUJO5frtejtYP04p2Y1PGN4vvcc"; //"AIzaSyDnadiiUUf05DtCkvw0LjUDJEYpXyu5hJU";

    public static final String KEY_GCM_REG_ID = EXTRA_BASE + "gcm.reg.id";
    public static final String KEY_OAUTH_TOKEN_URL = EXTRA_BASE + "oauth.token.url";

    public static final String KEY_GCM_PREFS = EXTRA_BASE + "prefs.";
    public static final String KEY_GCM_ALERTS = KEY_GCM_PREFS + "alerts";
    public static final String KEY_GCM_COMMANDS = KEY_GCM_PREFS + "commands";
    public static final String KEY_GCM_EVENTS = KEY_GCM_PREFS + "events";
    public static final String KEY_SERVICES_PORT = KEY_GCM_PREFS + "svcs.port";
    public static final String KEY_SERVICES_USE_SSL = KEY_GCM_PREFS + "svcs.ssl";
    public static final String KEY_NUM_RECENTS = KEY_GCM_PREFS + "num.recents";
    public static final String KEY_SELECTED_NAV_ITEM = KEY_GCM_PREFS + "nav.pos";
    public static final String KEY_SYNC_WIFI_ONLY = KEY_GCM_PREFS + "sync.wifi.only";

    public static final String KEY_REGISTRATION_ID = "gcmRegistrationId";
    public static final String KEY_GCM_REGISTERED = EXTRA_BASE + "gcmRegistered";
    public static final String KEY_APP_VERSION = EXTRA_BASE + "appVersion";
    public static final String KEY_RESULT = "result";
    public static final String KEY_ERRORS = "errors";
    public static final String KEY_CLIENT_ID = "oauthId";
    public static final String KEY_CLIENT_SECRET = "oauthSecret";


    public static final int DEF_OAUTH_PORT = 8089;
    public static final int DEF_NUM_RECENTS = 3;
    public static final int MIN_NUM_RECENTS = 1;
    public static final int MAX_NUM_RECENTS = 10;

    public static final String API_GATEWAY_CLIENT_ID = "561b0c7f-6cdf-4f94-9e16-a0ecaf08c382";  //"97b2578b-aa42-4866-b005-fd28b0656b88";
    public static final String API_GATEWAY_CLIENT_SECRET = "c0d79221-2489-49f7-89d0-7c56d0df05d6";  //"d6a228fa-2cad-4160-8b23-0b8b60adb0c6";

    public static final DialogInterface.OnClickListener NOOP_LISTENER = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        }
    };

    public static final String TAG_PROGRESS = "progFrag";
    public static final String TAG_SINGLE_PANE = "singleFrag";

}
