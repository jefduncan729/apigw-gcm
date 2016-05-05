package com.axway.apigwgcm.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.util.StringUtil;

import java.util.Locale;

public class BaseActivity extends Activity implements OnClickListener {
	private static final String TAG = BaseActivity.class.getSimpleName();

    protected static final int REQ_BASE = 1000;

	protected static final String TAG_PROG_DLG = "progDlg";
	protected static final String TAG_INFO_DLG = "infoDlg";
	protected static final String TAG_ALERT_DLG = "alertDlg";
	protected static final String TAG_CONFIRM_DLG = "confirmDlg";

    protected static final int FRAG_TRANSITION = FragmentTransaction.TRANSIT_FRAGMENT_FADE;
	
	private SharedPreferences prefs;
    private ProgressBar progressBar;
    protected Toolbar toolbar;
    private Handler.Callback handlerCallback;
    protected Handler handler;
    private boolean fromSavedState;
    private ConnectivityManager connMgr;
    protected BaseApp baseApp = BaseApp.getInstance();

	public BaseActivity() {
		super();
		prefs = null;
        handlerCallback = null;
        connMgr = null;
        handler = null;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromSavedState = (savedInstanceState != null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG, "setContentView");
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (toolbar != null) {
            setupToolbar();
            setActionBar(toolbar);
            addNavListener();
        }
    }

    protected void setupToolbar() {
        Log.d(TAG, "setupToolbar");
        //do nothing
    }

    protected boolean navIsBack() {
        return false;
    }

    protected void addNavListener() {
        if (navIsBack()) {
            toolbar.setNavigationIcon(R.mipmap.ic_action_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "nav button clicked, go back");
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        Log.d(TAG, StringUtil.format("setTitle: %s", title));
        super.setTitle(title);
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    public void setSubtitle(String s) {
        if (toolbar == null)
            return;
        toolbar.setSubtitle(s);
    }

    protected SharedPreferences getPrefs() {
		if (prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs;
	}

	protected void cancelTask(AsyncTask<?, ?, ?> task) {
		if (task == null)
			return;
		task.cancel(true);
	}
	
	public void showSettings(Bundle extras) {
		Intent i = new Intent(this, SettingsActivity.class);
        if (extras != null)
            i.putExtras(extras);
		startActivityForResult(i, R.id.action_settings);
	}
/*
	protected void showProgressDialog() {
		showProgressDialog(null, getString(R.string.confirm_msg));
	}

	protected void showProgressDialog(String message) {
		showProgressDialog(null, message);
	}

	protected void showProgressDialog(String title, String message) {
        final ProgressDialog dlg = new ProgressDialog(this);
        if (title != null)
            dlg.setTitle(title);
        if (TextUtils.isEmpty(message))
            message = "Loading...";
        dlg.setMessage(message);
        dlg.setIndeterminate(true);
        dlg.setCancelable(false);
        dlg.show();
	}

	protected void dismissProgressDialog() {
		DialogFragment frag = (DialogFragment)getFragmentManager().findFragmentByTag(TAG_PROG_DLG);
		if (frag != null)
			frag.dismiss();
	}
	
	protected boolean progressDialogShowing() {
		DialogFragment frag = (DialogFragment)getFragmentManager().findFragmentByTag(TAG_PROG_DLG);
		return (frag != null);
	}

	protected void confirmDialog(DialogInterface.OnClickListener onYes) {
		confirmDialog(getString(R.string.confirm_msg), onYes);
	}
*/
    public void confirmDialog(final String msg, final DialogInterface.OnClickListener onYes) {
		confirmDialog(getString(R.string.confirm), msg, onYes);
	}

	public void confirmDialog(final String title, final String msg, final DialogInterface.OnClickListener onYes) {
		confirmDialog(title, msg, onYes, null);
	}

	public void confirmDialog(final String title, final String msg, final DialogInterface.OnClickListener onYes, final DialogInterface.OnClickListener onNo) {
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setMessage(msg)
                .setTitle(title)
//                .setIcon(R.mipmap.ic_action_help)
                .setCancelable(false);
        bldr.setPositiveButton(android.R.string.yes, onYes == null ? Constants.NOOP_LISTENER : onYes);
        bldr.setNegativeButton(android.R.string.no, onNo == null ? Constants.NOOP_LISTENER : onNo);
        AlertDialog dlg = bldr.create();
        dlg.show();
	}
	
	public void alertDialog(final String msg) {
		alertDialog(msg, "");
	}

	public void alertDialog(final String msg, final DialogInterface.OnClickListener onYes) {
		alertDialog(getString(R.string.alert), msg, onYes);
	}

    public void alertDialog(final String title, final String msg) {
        alertDialog(title, msg, null);
    }

    public void alertDialog(final String title, final String msg, final DialogInterface.OnClickListener onYes) {
		alertDialog(title, msg, onYes, null);
	}	
	
    public void alertDialog(final String title, final String msg, final DialogInterface.OnClickListener onYes, final DialogInterface.OnClickListener onNo) {
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setMessage(msg)
            .setTitle(title)
//            .setIcon(R.mipmap.ic_action_warning)
            .setCancelable(false);
        bldr.setPositiveButton(android.R.string.yes, onYes == null ? Constants.NOOP_LISTENER : onYes);
        if (onNo != null)
            bldr.setNegativeButton(android.R.string.no, onNo);
        AlertDialog dlg = bldr.create();
        dlg.show();
	}
	
//	@Override
    public void alertDialog(final String title, final String msg, final DialogInterface.OnClickListener onYes, final DialogInterface.OnClickListener onNo, final DialogInterface.OnClickListener onNeutral) {
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setMessage(msg)
                .setTitle(title)
  //              .setIcon(R.mipmap.ic_action_warning)
                .setCancelable(false);
        bldr.setPositiveButton(android.R.string.yes, onYes == null ? Constants.NOOP_LISTENER : onYes);
        if (onNo != null)
            bldr.setNegativeButton(android.R.string.no, onNo);
        if (onNeutral != null)
            bldr.setNeutralButton(android.R.string.cut, onNeutral);
        AlertDialog dlg = bldr.create();
        dlg.show();
	}

//    @Override
	public void infoDialog(final String msg) {
		infoDialog(getString(R.string.info), msg);
	}

//    @Override
    public void infoDialog(final String title, final String msg) {
		infoDialog(title, msg, null);
	}

//    @Override
    public void infoDialog(final String title, final String msg, final DialogInterface.OnClickListener onYes) {
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setMessage(msg)
                .setTitle(title)
//                .setIcon(R.mipmap.ic_action_about)
                .setCancelable(false);
        bldr.setPositiveButton(android.R.string.yes, onYes == null ? Constants.NOOP_LISTENER : onYes);
        AlertDialog dlg = bldr.create();
        dlg.show();
	}
	
	protected void handleException(Exception e) {
        hideProgressFrag();
		String msg = null;
		if (e != null)
			msg = e.getLocalizedMessage();
		if (msg == null)
			msg = "unknown exception";
		Log.e(TAG, msg, e);
		System.gc();
	}

	@Override
	public void onClick(View arg0) {
	}

    public boolean isMultiPane() {
        return false;
    }

    protected boolean onHandleMessage(Message msg) {
        return false;
    }

    private Handler.Callback getHandlerCallback() {
        if (handlerCallback == null) {
            handlerCallback = new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    return onHandleMessage(message);
                }
            };
        }
        return handlerCallback;
    }

    protected void replaceFragment(int ctrId, Fragment frag, String tag) {
        getFragmentManager().beginTransaction().replace(ctrId, frag, tag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    protected Fragment findFragment(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

    protected Fragment findFragment(int ctrId) {
        return getFragmentManager().findFragmentById(ctrId);
    }

    protected void addFragment(int ctrId, Fragment frag, String tag) {
        getFragmentManager().beginTransaction().add(ctrId, frag, tag).setTransition(FRAG_TRANSITION).commit();
    }

    protected void removeFragment(String tag) {
        Fragment frag = findFragment(tag);
        if (frag == null)
            return;
        getFragmentManager().beginTransaction().remove(frag).setTransition(FRAG_TRANSITION).commit();
    }

    protected void removeFragment(int id) {
        Fragment frag = findFragment(id);
        if (frag == null)
            return;
        getFragmentManager().beginTransaction().remove(frag).setTransition(FRAG_TRANSITION).commit();
    }

    protected void hideFragment(String tag) {
        Fragment frag = findFragment(tag);
        if (frag == null)
            return;
        getFragmentManager().beginTransaction().hide(frag).setTransition(FRAG_TRANSITION).commit();
    }

    protected void hideFragment(int id) {
        Fragment frag = findFragment(id);
        if (frag == null)
            return;
        getFragmentManager().beginTransaction().hide(frag).setTransition(FRAG_TRANSITION).commit();
    }
/*
    public String getSafeExtra(String key, String defVal) {
        if (getSafeExtras().containsKey(key))
            return getSafeExtras().getString(key);
        return defVal;
    }

    public String getSafeExtra(String key) {
        return getSafeExtra(key, "");
    }

    public int getSafeExtra(String key, int defVal) {
        if (getSafeExtras().containsKey(key))
            return getSafeExtras().getInt(key);
        return defVal;
    }

    public long getSafeExtra(String key, long defVal) {
        if (getSafeExtras().containsKey(key))
            return getSafeExtras().getLong(key);
        return defVal;
    }

    public Bundle getSafeExtras() {
        if (safeExtras == null)
            safeExtras = new Bundle();
        return safeExtras;
    }
*/
    public void showProgressFrag(String msg) {
        showProgressFrag(msg, R.id.container01);
    }

    protected void showProgressFrag(String msg, int ctrId) {
//        ProgressFragment progFrag = new ProgressFragment();
//        Bundle args = new Bundle();
//        if (!TextUtils.isEmpty(msg))
//            args.putString(Intent.EXTRA_TEXT, msg);
//        progFrag.setArguments(args);
//        if (!isMultiPane())
//            ctrId = R.id.container01;
//        replaceFragment(ctrId, progFrag, Constants.TAG_PROGRESS);
    }

    protected void hideProgressFrag() {
        hideFragment(Constants.TAG_PROGRESS);
    }

//    @Override
    public void showToast(int msgId) {
        showToast(msgId, Toast.LENGTH_SHORT);
    }

//    @Override
    public void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    protected void showToast(int msgId, int duration) {
        runOnUiThread(new ToastRunnable(msgId, duration));
    }

    protected void showToast(String msg, int duration) {
        runOnUiThread(new ToastRunnable(msg, duration));
    }

    private void primShowToast(int msgId, int duration) {
        Toast.makeText(this, msgId, duration).show();
    }

    private void primShowToast(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    private class ToastRunnable implements Runnable {

        private int msgId;
        private String msg;
        private int duration;

        protected ToastRunnable() {
            super();
            msgId = -1;
            msg = null;
            duration = Toast.LENGTH_SHORT;
        }

        public ToastRunnable(int msgId, int duration) {
            this();
            this.duration = duration;
            this.msgId = msgId;
        }

        public ToastRunnable(String msg, int duration) {
            this();
            this.duration = duration;
            this.msg = msg;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(msg))
                if (msgId == -1)
                    return;
                else
                    primShowToast(msgId, duration);
            else
                primShowToast(msg, duration);
        }
    }

    protected ProgressBar getProgressBar() {
        if (progressBar == null)
            progressBar = (ProgressBar)findViewById(android.R.id.progress);
        return progressBar;
    }

//    @Override
    public void showProgressBar(final boolean show) {
        if (getProgressBar() == null)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    protected Bundle intentToBundle() {
        final Bundle rv = new Bundle();
        final Intent i = getIntent();
        if (i != null) {
            rv.putString(Constants.EXTRA_ACTION, i.getAction());
            rv.putParcelable(Intent.EXTRA_UID, i.getData());
            if (i.getExtras() != null)
                rv.putAll(i.getExtras());
        }
        return rv;
    }
    public boolean isFromSavedState() {
        return fromSavedState;
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

    protected Handler getMsgHandler() {
        if (handler == null)
            handler = new Handler(getHandlerCallback());
        return handler;
    }
}
