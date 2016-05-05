package com.axway.apigwgcm.util;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.Constants;

/**
 * Created by su on 12/18/2014.
 */
public class GcmUtil {

    private static final String TAG = GcmUtil.class.getSimpleName();

/*
    private WeakReference<Context> ctxRef;

    public GcmUtil(final Context ctx) {
        super();
        ctxRef = (ctx == null ? null : new WeakReference<Context>(ctx));
    }

    private Context getContext() {
        if (ctxRef == null)
            return null;
        return ctxRef.get();
    }
*/
    public static String getRegistrationId(final SharedPreferences prefs) {
        if (prefs == null)
            return null;
        //TODO: check app version
        String rv = prefs.getString(Constants.KEY_REGISTRATION_ID, "");
        Log.d(TAG, "regId from prefs: " + rv);
        return rv;
    }

    public static void removeRegistrationId(final SharedPreferences prefs) {
        if (prefs == null)
            return;
        prefs.edit()
                .remove(Constants.KEY_REGISTRATION_ID)
                .apply();
        Log.d(TAG, "registration id removed");
    }

    public static void storeRegistrationId(final SharedPreferences prefs, final String regId) {
        if (prefs == null || TextUtils.isEmpty(regId))
            return;
        prefs.edit()
                .putString(Constants.KEY_REGISTRATION_ID, regId)
                .apply();
        Log.d(TAG, "registration id stored: regId");
    }

    public static boolean needGcmRegistration(final SharedPreferences prefs) {
        return TextUtils.isEmpty(getRegistrationId(prefs));
    }

    public static Bundle loadSettings(final SharedPreferences prefs) {
        if (prefs == null)
            return null;
        Bundle rv = new Bundle();
        String key = Constants.KEY_GCM_ALERTS;
        rv.putBoolean(key, prefs.getBoolean(key, false));
        key = Constants.KEY_GCM_COMMANDS;
        rv.putBoolean(key, prefs.getBoolean(key, false));
        key = Constants.KEY_GCM_EVENTS;
        rv.putBoolean(key, prefs.getBoolean(key, false));
        key = Constants.KEY_SERVICES_PORT;
        rv.putInt(key, prefs.getInt(key, 7080));
        key = Constants.KEY_SERVICES_USE_SSL;
        rv.putBoolean(key, prefs.getBoolean(key, false));
        return rv;
    }

    public static boolean saveSettings(final SharedPreferences prefs, final Bundle b) {
        if (prefs == null || b == null)
            return false;
        SharedPreferences.Editor edit = prefs.edit();
//        edit.putString(Constants.KEY_REGISTRATION_ID, b.getString(Constants.KEY_REGISTRATION_ID));
        String key = Constants.KEY_GCM_ALERTS;
        edit.putBoolean(key, b.getBoolean(key, false));
        key = Constants.KEY_GCM_COMMANDS;
        edit.putBoolean(key, b.getBoolean(key, false));
        key = Constants.KEY_GCM_EVENTS;
        edit.putBoolean(key, b.getBoolean(key, false));
        key = Constants.KEY_SERVICES_PORT;
        edit.putInt(key, b.getInt(key, 7080));
        key = Constants.KEY_SERVICES_USE_SSL;
        edit.putBoolean(key, b.getBoolean(key, false));
        edit.apply();
        return true;
    }
}
