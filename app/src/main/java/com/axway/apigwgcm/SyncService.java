package com.axway.apigwgcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by su on 12/27/2014.
 */
public class SyncService extends Service {

    private static final String TAG = SyncService.class.getSimpleName();

    private static SyncAdapter adapter;
    private static final Object syncLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (syncLock) {
            if (adapter == null) {
                adapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return adapter.getSyncAdapterBinder();
    }
}
