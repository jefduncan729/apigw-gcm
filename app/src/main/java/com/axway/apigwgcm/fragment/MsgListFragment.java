package com.axway.apigwgcm.fragment;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.AdapterView;

import com.axway.apigwgcm.R;
import com.axway.apigwgcm.activity.MessageDetailActivity;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.view.CursorViewBinder;
import com.axway.apigwgcm.view.EventViewBinder;

/**
 * Created by su on 12/22/2014.
 */
public class MsgListFragment extends CursorListFragment {

    private static final String TAG = MsgListFragment.class.getSimpleName();

    public MsgListFragment() {
        super();
    }

    public static MsgListFragment newInstance(Uri uri) {
        MsgListFragment rv = new MsgListFragment();
        rv.setPrimaryUri(uri);
        return rv;
    }

    @Override
    protected CursorViewBinder createViewBinder() {
        CursorViewBinder rv = null;
        int dbType = DbHelper.matchUri(getPrimaryUri());
        if (DbHelper.EVENTS == dbType)
            rv = new EventViewBinder();
        else
            rv = new CursorViewBinder();
        rv.setIconId(DbHelper.getIconId(dbType));
        return rv;
    }

    @Override
    protected String getEmptyTextString() {
        String t = DbHelper.getTitle(getPrimaryUri());
        if (TextUtils.isEmpty(t))
            return super.getEmptyTextString();
        return "No " + t;
    }

/*
    @Override
    protected String[] getProjection() {
        return DbHelper.MsgColumns.DEF_PROJECTION;
    }
*/

    @Override
    protected String getSortOrder() {
        return DbHelper.MsgColumns.DEF_SORT_ORDER;
    }

    @Override
    protected boolean canDelete() {
        return true;
    }

    @Override
    protected boolean canDeleteAll() {
        return true;
    }

    @Override
    protected int getNameColumnIndex() {
        return DbHelper.MsgColumns.NDX_SUBJECT;
    }

    @Override
    protected int getMenuLayoutId() {
        return R.menu.trigger_list;
    }

}
