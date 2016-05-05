package com.axway.apigwgcm.fragment;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.events.ActionEvent;
import com.axway.apigwgcm.util.StringUtil;
import com.axway.apigwgcm.view.CursorViewBinder;
import com.axway.apigwgcm.view.TriggerViewBinder;

import java.util.Locale;

/**
 * Created by su on 12/22/2014.
 */
public class TriggerListFragment extends CursorListFragment {

    private static final String TAG = TriggerListFragment.class.getSimpleName();

    public TriggerListFragment() {
        super();
    }

    public static TriggerListFragment newInstance() {
        TriggerListFragment rv = new TriggerListFragment();
        rv.setPrimaryUri(DbHelper.TriggerColumns.CONTENT_URI);
        return rv;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo)menuInfo;
        Cursor c = ((Cursor)getListView().getItemAtPosition(cmi.position));
        if (c == null)
            return;
        final Uri uri = ContentUris.withAppendedId(getPrimaryUri(), cmi.id);
        String name = c.getString(getNameColumnIndex());
        int stat = c.getInt(DbHelper.CommonColumns.NDX_STATUS);
        int p = menu.size() + 1;
        int action = R.id.action_enable;
        int strId = R.string.action_enable;
        if (stat == DbHelper.STATUS_ENABLED) {
            action = R.id.action_disable;
            strId = R.string.action_disable;
        }
        final MenuItem item = menu.add(0, action, p, strId);
        final Intent i = new Intent();
        i.setAction(getString(strId));
        i.setData(uri);
        item.setIntent(i);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Intent i = item.getIntent();
        if (i != null) {
            final int act = item.getItemId();
            if (R.id.action_disable == act || R.id.action_enable == act) {
                BaseApp.post(new ActionEvent(act, i));
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public int getMenuLayoutId() {
        return R.menu.trigger_list;
    }

    @Override
    protected boolean canDelete() {
        return true;
    }

    @Override
    protected boolean canEdit() {
        return true;
    }

//    @Override
    protected boolean canAdd() {
        return true;
    }

    @Override
    protected CursorViewBinder createViewBinder() {
        TriggerViewBinder rv = new TriggerViewBinder();
        rv.setIconId(R.mipmap.ic_nav_action_attachment);
        return rv;
    }

    @Override
    protected String getEmptyTextString() {
        return "No Triggers Defined";
    }

    //    @Override
    protected int getNameColumnIndex() {
        return DbHelper.TriggerColumns.NDX_NAME;
    }

    @Override
    protected String[] getProjection() {
        return DbHelper.TriggerColumns.DEF_PROJECTION;
    }

    @Override
    protected String getSortOrder() {
        return DbHelper.TriggerColumns.DEF_SORT_ORDER;
    }

    @Override
    protected String getSelection(Bundle args) {
        return StringUtil.format("%s <> ?", DbHelper.TriggerColumns.FLAG);
    }

    @Override
    protected String[] getSelectionArgs(Bundle args) {
        return new String[] { Integer.toString(DbHelper.FLAG_DELETED)};
    }

    @Override
    protected boolean canSync() {
        return true;
    }
}
