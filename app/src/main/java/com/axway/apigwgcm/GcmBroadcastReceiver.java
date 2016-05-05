package com.axway.apigwgcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by su on 11/15/2014.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GCM Broadcast received");
        ComponentName name = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(name)));
        setResultCode(Activity.RESULT_OK);
/*
        if (intent == null)
            return;
        Intent i = new Intent(context, GcmMessageActivity.class);
        if (intent.getExtras() != null) {
            i.putExtras(intent.getExtras());
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
*/
    }
}
