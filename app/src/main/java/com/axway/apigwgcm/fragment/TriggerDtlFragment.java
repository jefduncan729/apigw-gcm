package com.axway.apigwgcm.fragment;

import android.net.Uri;

import com.axway.apigwgcm.db.DbHelper;

/**
 * Created by su on 12/22/2014.
 */
public class TriggerDtlFragment extends CursorFragment {

    private static final String TAG = TriggerDtlFragment.class.getSimpleName();

    public TriggerDtlFragment() {
        super();
    }

    public static TriggerDtlFragment newInstance(final Uri uri) {
        TriggerDtlFragment rv = new TriggerDtlFragment();
        rv.setPrimaryUri(uri);
        return rv;
    }

    @Override
    protected String[] getProjection() {
        return DbHelper.TriggerColumns.DEF_PROJECTION;
    }

    @Override
    protected String getSortOrder() {
        return DbHelper.TriggerColumns.DEF_SORT_ORDER;
    }
}
