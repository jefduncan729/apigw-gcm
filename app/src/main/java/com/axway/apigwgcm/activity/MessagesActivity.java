package com.axway.apigwgcm.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.BaseListFragment;
import com.axway.apigwgcm.fragment.CursorListFragment;
import com.axway.apigwgcm.fragment.MessageDtlFragment;
import com.axway.apigwgcm.fragment.MsgListFragment;
import com.axway.apigwgcm.util.ListCallbacks;

/**
 * Created by su on 12/5/2014.
 */
public class MessagesActivity extends AbstractListActivity {

    private static final String TAG = MessagesActivity.class.getSimpleName();

    @Override
    protected ListFragment createListFrag(final Intent intent) {
        setTitle(title);
        MsgListFragment f = new MsgListFragment();
        f.setArguments(intentToBundle());
        return f;
    }

    @Override
    protected Fragment createDetailFrag(final Intent intent) {
        Uri uri = (intent == null ? null : intent.getData());
        if (uri == null)
            return null;
        setTitle(DbHelper.getTitle(uri));
        return MessageDtlFragment.newInstance(uri);
    }
}
