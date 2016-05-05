package com.axway.apigwgcm.activity;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.events.ActionEvent;
import com.axway.apigwgcm.fragment.TriggerDtlFragment;
import com.axway.apigwgcm.fragment.TriggerEditFragment;
import com.axway.apigwgcm.fragment.TriggerListFragment;
import com.axway.apigwgcm.util.StringUtil;
import com.squareup.otto.Subscribe;

import java.util.Locale;

/**
 * Created by su on 12/5/2014.
 */
public class TriggersActivity extends AbstractListActivity {

    private static final String TAG = TriggersActivity.class.getSimpleName();

    @Override
    protected void onResume() {
        super.onResume();
        BaseApp.bus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseApp.bus().unregister(this);
    }

    @Override
    protected Uri provideUri() {
        Uri rv = super.provideUri();
        if (rv == null) {
            rv = DbHelper.TriggerColumns.CONTENT_URI;
            getIntent().setData(rv);
        }
        return rv;
    }

    @Override
    protected Fragment createEditFrag(final Intent intent) {
        setTitle(StringUtil.format("%s Trigger", mode == MODE_EDIT ? "Modify" : "Create"));
        return TriggerEditFragment.newInstance(mode == MODE_EDIT ? intent.getData() : null);
    }

    @Override
    protected Fragment createDetailFrag(Intent intent) {
        if (intent == null || intent.getData() == null)
            return null;
        return TriggerDtlFragment.newInstance(intent.getData());
    }

    @Override
    protected ListFragment createListFrag(final Intent intent) {
        setTitle(title);    //"Event Triggers");
        return TriggerListFragment.newInstance();
    }

    @Override
    protected Uri insertItem(Bundle data) {
        ContentValues cv = new ContentValues();
        long now = System.currentTimeMillis();
        String msg = null;
        cv.put(DbHelper.TriggerColumns.EXPRESSION, data.getString(DbHelper.TriggerColumns.EXPRESSION));
        cv.put(DbHelper.TriggerColumns.NAME, data.getString(DbHelper.TriggerColumns.NAME));
        cv.put(DbHelper.TriggerColumns.TYPE, data.getInt(DbHelper.TriggerColumns.TYPE));
        cv.put(DbHelper.TriggerColumns.MODIFY_DATE, now);
        cv.put(DbHelper.TriggerColumns.CREATE_DATE, now);
        cv.put(DbHelper.TriggerColumns.FLAG, DbHelper.FLAG_NEW);
        cv.put(DbHelper.TriggerColumns.STATUS, data.getInt(DbHelper.TriggerColumns.STATUS));
        return getContentResolver().insert(DbHelper.TriggerColumns.CONTENT_URI, cv);
    }

    @Override
    protected int updateItem(Bundle data) {
        ContentValues cv = new ContentValues();
        long now = System.currentTimeMillis();
        String msg = null;
        cv.put(DbHelper.TriggerColumns.EXPRESSION, data.getString(DbHelper.TriggerColumns.EXPRESSION));
        cv.put(DbHelper.TriggerColumns.NAME, data.getString(DbHelper.TriggerColumns.NAME));
        cv.put(DbHelper.TriggerColumns.TYPE, data.getInt(DbHelper.TriggerColumns.TYPE));
        cv.put(DbHelper.TriggerColumns.MODIFY_DATE, now);
        cv.put(DbHelper.TriggerColumns.FLAG, DbHelper.FLAG_UPDATED);
        cv.put(DbHelper.TriggerColumns.STATUS, data.getInt(DbHelper.TriggerColumns.STATUS));
        Uri uri = data.getParcelable(Intent.EXTRA_UID);
        int rv = 0;
        if (uri != null) {
            rv = getContentResolver().update(uri, cv, null, null);
        }
        return rv;
    }

    @Override
    protected boolean editWhenSelected() {
        return true;
    }

    @Subscribe
    public void onActionEvent(ActionEvent evt) {
        if (evt.intent == null || evt.intent.getData() == null)
            return;
        ContentValues cv = new ContentValues();
        switch (evt.id) {
            case R.id.action_enable:
                cv.put(DbHelper.TriggerColumns.STATUS, DbHelper.STATUS_ENABLED);
                break;
            case R.id.action_disable:
                cv.put(DbHelper.TriggerColumns.STATUS, DbHelper.STATUS_DISABLED);
                break;
        }
        if (cv.size() > 0) {
            cv.put(DbHelper.TriggerColumns.MODIFY_DATE, System.currentTimeMillis());
            int n = getContentResolver().update(evt.intent.getData(), cv, null, null);
        }
    }
}
