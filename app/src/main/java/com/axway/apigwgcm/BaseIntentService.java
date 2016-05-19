package com.axway.apigwgcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.JsonUtil;

abstract public class BaseIntentService extends IntentService {

    private static final String TAG = BaseIntentService.class.getSimpleName();

    public static final String ACTION_BASE = "axway.gcm.demo.";
	public static final String ACTION_KILL_RES_RCVR = ACTION_BASE + "killResRcvr";

	private SharedPreferences prefs;
	private NotificationManager notificationMgr;
    private Notification.Builder notificationBldr;
	private Handler handler;
    private Handler.Callback handlerCallback;
    protected Context context;
	private ResultReceiver resRcvr;
    private ConnectivityManager connMgr;
	protected BaseApp baseApp;
    protected AccountUtil acctUtil;
    protected JsonUtil jsonUtil;

    private class DisplayToast implements Runnable {

		String msg;
		int len;
		
		public DisplayToast(String msg) {
			this(msg, Toast.LENGTH_SHORT);
		}

		public DisplayToast(String msg, int len) {
			super();
			this.msg = msg;
			this.len = len;
		}
		
		@Override
		public void run() {
			if (len != Toast.LENGTH_LONG && len != Toast.LENGTH_SHORT)
				len = Toast.LENGTH_SHORT;
    		Toast.makeText(context, msg, len).show();
		}
	}

	protected BaseIntentService(String name) {
		super(name);
		prefs = null;
		notificationMgr = null;
        notificationBldr = null;
		handler = null;
		context = null;
		resRcvr = null;
        baseApp = null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
        baseApp = BaseApp.getInstance();
        acctUtil = AccountUtil.getInstance();
        jsonUtil = JsonUtil.getInstance();
		context = this;
		handler = null;
    }

	@Override
	public void onDestroy() {
		handler = null;
		notificationMgr = null;
		prefs = null;
		context = null;
		super.onDestroy();
	}

	protected void showToast(String msg) {
		getHandler().post(new DisplayToast(msg));
	}

	protected void showToastLong(String msg) {
		getHandler().post(new DisplayToast(msg, Toast.LENGTH_LONG));
	}

	protected SharedPreferences getPrefs() {
		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		}
		return prefs;
	}
	
	protected NotificationManager getNotificationManager() {
		if (notificationMgr == null)
			notificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		return notificationMgr;
	}

    protected Notification.Builder getNotificationBuilder() {
        if (notificationBldr == null)
            notificationBldr = new Notification.Builder(this);
        return notificationBldr;
    }

    @Override
	protected void onHandleIntent(final Intent intent) {
		if (ACTION_KILL_RES_RCVR.equals(intent.getAction()))
			resRcvr = null;
		else
			resRcvr = intent.getParcelableExtra(Intent.EXTRA_RETURN_RESULT);
	}

    protected boolean onHandleMessage(final Message msg) {
        //default implementation does nothing
        return true;
    }

    protected Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getHandlerCallback());
        }
        return handler;
    }

    private Handler.Callback getHandlerCallback() {
        if (handlerCallback == null) {
            handlerCallback = new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    return onHandleMessage(msg);
                }
            };
        }
        return handlerCallback;
    }

    protected void sendResult(final int code) {
        sendResult(code, null);
    }

    protected void sendResult(final int code, final Bundle data) {
        if (getResultReceiver() == null) {
            Log.d(TAG, "sendResult: no resultReceiver");
            return;
        }
        getResultReceiver().send(code, data);
    }

	protected ResultReceiver getResultReceiver() {
		return resRcvr;
	}

    protected ConnectivityManager getConnectivityMgr() {
        if (connMgr == null) {
            connMgr = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        }
        return connMgr;
    }

    private NetworkInfo getNetworkInfo() {
        return getConnectivityMgr().getActiveNetworkInfo();
    }

    private boolean haveNetwork(int typ) {
        final NetworkInfo info = getNetworkInfo();
        if (info == null)
            return false;
        return (info.getType() == typ && info.isConnectedOrConnecting());
    }

    protected boolean haveWifiNetwork() {
        return haveNetwork(ConnectivityManager.TYPE_WIFI);
    }

    protected boolean haveMobileNetwork() {
        return haveNetwork(ConnectivityManager.TYPE_MOBILE);
    }
}
