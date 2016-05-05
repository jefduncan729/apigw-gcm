package com.axway.apigwgcm.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by su on 11/15/2014.
 */
public class AuthService extends Service {

    private Authenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
