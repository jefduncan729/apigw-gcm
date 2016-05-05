package com.axway.apigwgcm.activity;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.SyncAdapter;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.BaseListFragment;
import com.axway.apigwgcm.fragment.CursorFragment;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.EditCallbacks;
import com.axway.apigwgcm.util.ListCallbacks;
import com.axway.apigwgcm.util.StringUtil;

import java.util.Locale;

/**
 * Created by su on 12/5/2014.
 */
abstract public class BaseListActivity extends BaseActivity implements ListCallbacks, EditCallbacks {

    private static final String TAG = BaseListActivity.class.getSimpleName();
    private static final int MSG_DO_SYNC = 5001;

    public static final int MODE_LIST = 1;
    public static final int MODE_DETAIL = 2;
    public static final int MODE_EDIT = 3;
    public static final int MODE_INSERT = 4;

    protected String title;
    protected int mode;
    private ContentObserver observer;
    private BroadcastReceiver bcastRcvr;

    protected int getLayoutId() {
        return R.layout.tb_frame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        Uri uri = provideUri();
        if (uri == null) {
            showToast("invalid input: no uri specified or provided");
            finish();
            return;
        }
        title = DbHelper.getTitle(uri); //getIntent().getStringExtra(Intent.EXTRA_TITLE);
//        if (!TextUtils.isEmpty(title))
//            setTitle(title);
        showListFrag();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bnd = (data == null ? null : data.getExtras());
            String verb = null;
            switch (requestCode) {
                case R.id.action_add:
                    if (bnd != null && insertItem(bnd) != null)
                        verb = "added";
                    break;
                case R.id.action_edit:
                    if (bnd != null && updateItem(bnd) > 0)
                        verb = "updated";
                    break;
            }
            if (!TextUtils.isEmpty(verb)) {
                showToast(StringUtil.format("%s %s", bnd.getString(DbHelper.TriggerColumns.NAME), verb));
                showListFrag();
            }
        }
    }

    protected Uri provideUri() {
        return (getIntent() == null ? null : getIntent().getData());
    }

    protected boolean editWhenSelected() {
        return false;
    }

    protected boolean useActivities() {
        return true;
    }

    @Override
    public void onItemSelected(Intent intent) {
        if (intent == null)
            return;
        Uri uri = intent.getData();
        if (uri == null)
            uri = intent.getParcelableExtra(Intent.EXTRA_UID);
        if (uri == null)
            return;
        if (editWhenSelected()) {
            intent.setAction(Intent.ACTION_EDIT);
            onEditItem(intent);
        }
        else {
            intent.setAction(Intent.ACTION_VIEW);
            onViewItem(intent);
        }
    }

    @Override
    public void onSetRowCount(final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (title != null) {
                    setTitle(title);
                    setSubtitle(StringUtil.format("%d row%s", count, (count == 1 ? "" : "s")));
                }
            }
        });
    }

    @Override
    public void onDelete(final Intent intent) {
        if (intent == null)
            return;
        final Uri uri = intent.getData();   //getParcelableExtra(Intent.EXTRA_UID);
        final String name = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (uri == null || TextUtils.isEmpty(name))
            return;
        confirmDelete(uri, name);
    }

    @Override
    public void onAddItem(Intent intent) {
        if (intent == null) {
            return;
        }
        mode = MODE_INSERT;
        if (useActivities()) {
            startActivityForResult(intent, R.id.action_add);
        }
        else {
            showEditFrag(null);
        }
    }

    @Override
    public void onEditItem(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }
        mode = MODE_EDIT;
        if (useActivities()) {
            startActivityForResult(intent, R.id.action_edit);
        }
        else {
            showEditFrag(intent.getData());
        }
    }

    protected void onViewItem(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }
        mode = MODE_DETAIL;
        if (useActivities()) {
            startActivity(intent);
        }
        else {
            showDetailFrag(intent.getData());
        }
    }

    protected void confirmDelete(final Uri uri, final String name) {
        String msg = getString(R.string.confirm_msg, "delete " + name);
        confirmDialog(msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performDelete(uri, name);
            }
        });
    }

    protected void performDelete(final Uri uri, final String name) {
        getContentResolver().delete(uri, null, null);
        showToast(name + " deleted");
    }

    private void registerObserver() {
        final Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d(TAG, "registering ContentObserver for " + uri.toString());
            getContentResolver().registerContentObserver(uri, true, getContentObserver());
        }
    }

    private void unregisterObservers() {
        if (observer != null) {
            Log.d(TAG, "unregistering ContentObservers");
            getContentResolver().unregisterContentObserver(observer);
        }
    }

    protected void contentChanged(final boolean selfChange, final Uri uri) {
        Log.d(TAG, "contentChanged");
        final Fragment frag = findFragment(R.id.container01);
        if (frag != null && frag instanceof BaseListFragment) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((BaseListFragment)frag).refresh();
                }
            });
        }
//        requestSync(uri);
    }

    private void requestSync(final Uri uri) {
        if (uri == null)
            return;
        final Intent i = new Intent();
        i.setDataAndType(uri, DbHelper.getMimeType(uri));
        onRequestSync(i);
    }

    @Override
    public void onRequestSync(final Intent i) {
        if (!haveWifiNetwork()) {
            if (getPrefs().getBoolean(Constants.KEY_SYNC_WIFI_ONLY, true)) {
                showToast(R.string.no_wifi_network);
                return;
            }
            if (!haveMobileNetwork()) {
                showToast(R.string.no_network);
                return;
            }
        }
        SyncRequest.Builder bldr = new SyncRequest.Builder();
//        SyncRequest sr = new SyncRequest.Builder()
        bldr.setExpedited(true);
        bldr.setManual(true);
        bldr.setNoRetry(true);
        bldr.setSyncAdapter(AccountUtil.getInstance().getSingleAccount(), DbHelper.CONTENT_AUTHORITY);
        bldr.syncOnce();
        bldr.setExtras(new Bundle());
        try {
            SyncRequest sr = bldr.build();
            ContentResolver.requestSync(sr);    //AccountUtil.getInstance(this).getSingleAccount(), DbHelper.CONTENT_AUTHORITY, b);
        }
        catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
/*
        Intent si = new Intent(this, MySyncService.class);
        si.setAction(SyncAdapter.ACTION_SYNC_COMPLETE);
        si.putExtra(Intent.EXTRA_RESULT_RECEIVER, getResRcvr());
        startService(si);
*/
    }

    protected void onSyncComplete(final Intent intent) {
        SyncResult res = null;
        if (intent != null)
            res = intent.getParcelableExtra("syncResult");
        Log.d(TAG, StringUtil.format("onSyncComplete: %s", (res == null ? "null" : res.toString())));
        showToast("Sync complete");
        showListFrag();
    }

    private ContentObserver getContentObserver() {
        if (observer == null) {
            observer = new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) {
                    onChange(selfChange, null);
                }

                @Override
                public void onChange(final boolean selfChange, final Uri uri) {
                    Log.d(TAG, "ContentObserver.onChange");
                    contentChanged(selfChange, uri);
                }
            };
        }
        return observer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter fltr = new IntentFilter();
        fltr.addAction(SyncAdapter.ACTION_SYNC_COMPLETE);
        fltr.addCategory(SyncAdapter.CATEGORY_SYNC);
        LocalBroadcastManager.getInstance(this).registerReceiver(getBcastRcvr(), fltr);
        registerObserver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bcastRcvr != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(bcastRcvr);
            bcastRcvr = null;
        }
        unregisterObservers();
    }

    protected BroadcastReceiver getBcastRcvr() {
        if (bcastRcvr == null) {
            bcastRcvr = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (SyncAdapter.ACTION_SYNC_COMPLETE.equals(intent.getAction())) {
                        onSyncComplete(intent);
                    }
                }
            };
        }
        return bcastRcvr;
    }

    abstract protected int updateItem(Bundle data);
    abstract protected Uri insertItem(Bundle data);

    abstract protected ListFragment createListFrag(Intent intent);
    abstract protected Fragment createEditFrag(Intent intent);
    abstract protected Fragment createDetailFrag(Intent intent);

    protected void showListFrag() {
        final Intent i = getIntent();
        if (i.getData() == null) {
            i.setData(provideUri());
        }
        ListFragment lf = createListFrag(i);
        if (lf == null)
            return;
        mode = MODE_LIST;
        replaceFragment(R.id.container01, lf, Constants.TAG_SINGLE_PANE);
    }

    protected void showEditFrag(Uri uri) {
        final Intent i = new Intent();
        if (uri == null) {
            mode = MODE_INSERT;
            i.setAction(Intent.ACTION_INSERT);
        }
        else {
            mode = MODE_EDIT;
            i.setAction(Intent.ACTION_EDIT);
            i.setData(uri);
        }
        Fragment f = createEditFrag(i);
        if (f == null)
            return;
        replaceFragment(R.id.container01, f, Constants.TAG_SINGLE_PANE);
    }

    protected void showDetailFrag(Uri uri) {
        if (uri == null) {
            return;
        }
        final Intent i = new Intent();
        mode = MODE_DETAIL;
        i.setAction(Intent.ACTION_VIEW);
        i.setData(uri);
        Fragment f = createDetailFrag(i);
        if (f == null)
            return;
        replaceFragment(R.id.container01, f, Constants.TAG_SINGLE_PANE);
    }

    @Override
    public void onSaveItem(Bundle data) {
        String verb = null;
        switch (mode) {
            case MODE_EDIT:
                if (updateItem(data) > 0)
                    verb = "updated";
                break;
            case MODE_INSERT:
                if (insertItem(data) != null)
                    verb = "added";
                break;
        }
        if (!TextUtils.isEmpty(verb)) {
            showToast(StringUtil.format("%s %s", data.getString(DbHelper.TriggerColumns.NAME), verb));
        }
        showListFrag();
    }

    @Override
    public void onEditCanceled() {
        confirmCancel();
    }

    @Override
    public void onValidationError(String msg) {
        if (!TextUtils.isEmpty(msg))
            showToast(msg);
    }

    @Override
    public void onBackPressed() {
        if (useActivities() || mode == MODE_LIST) {
            super.onBackPressed();
            return;
        }
        if (mode == MODE_DETAIL) {
            showListFrag();
            return;
        }
        confirmCancel();
    }

    protected void confirmCancel() {
        if (isFinishing())
            return;
        if (isDirty()) {
            confirmDialog(getString(R.string.touch_to_1, "discard changes"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    performCancel();
                }
            });
            return;
        }
        performCancel();
    }

    protected boolean isDirty() {
        if (useActivities() || mode == MODE_LIST)
            return false;
        Fragment f = findFragment(R.id.container01);
        if (f instanceof CursorFragment) {
            return ((CursorFragment)f).isDirty();
        }
        return false;
    }

    protected void performCancel() {
//        setResult(RESULT_CANCELED);
//        finish();
        if (isFinishing())
            return;
        if (mode == MODE_DETAIL || mode == MODE_LIST) {
            finish();
            return;
        }
        showToast(getString(R.string.op_cncld, (mode == MODE_EDIT ? "Edit" : "Insert")));
        showListFrag();
    }

    @Override
    protected boolean onHandleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DO_SYNC:
                Log.d(TAG, "sync requested");
                return true;
        }
        return super.onHandleMessage(msg);
    }
}
