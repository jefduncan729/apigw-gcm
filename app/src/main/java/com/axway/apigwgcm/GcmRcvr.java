package com.axway.apigwgcm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.axway.apigwgcm.util.StringUtil;
import com.google.android.gms.gcm.GcmReceiver;

import java.util.Locale;

/**
 * Created by su on 3/22/2016.
 */
public class GcmRcvr extends GcmReceiver {
    public static final String TAG = GcmRcvr.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, StringUtil.format("oncReceive: %s, %s", context, intent));
    }
}
