package com.axway.apigwgcm.fragment;

import android.net.Uri;

import com.axway.apigwgcm.db.DbHelper;

/**
 * Created by su on 4/20/2016.
 */
public class MessageDtlFragment extends CursorFragment {

    public MessageDtlFragment() {
        super();
    }

    public static MessageDtlFragment newInstance(final Uri uri) {
        MessageDtlFragment rv = new MessageDtlFragment();
        rv.setPrimaryUri(uri);
        return rv;
    }

    @Override
    protected String getSortOrder() {
        return DbHelper.EventColumns.DEF_SORT_ORDER;
    }
}
