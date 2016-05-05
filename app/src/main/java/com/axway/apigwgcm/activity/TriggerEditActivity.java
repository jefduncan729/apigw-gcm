package com.axway.apigwgcm.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.TriggerEditFragment;
import com.axway.apigwgcm.util.EditCallbacks;

/**
 * Created by su on 12/19/2014.
 */
public class TriggerEditActivity extends BaseActivity implements EditCallbacks {

    private static final String TAG = TriggerEditActivity.class.getSimpleName();
    private boolean isInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tb_frame);
        setResult(RESULT_CANCELED);
        isInsert = Intent.ACTION_INSERT.equals(getIntent().getAction());
        TriggerEditFragment frag = TriggerEditFragment.newInstance(isInsert ? null : getIntent().getData());
//        Bundle args = new Bundle();
//        frag.setArguments(args);
        replaceFragment(R.id.container01, frag, Constants.TAG_SINGLE_PANE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInsert)
            setTitle("Create Trigger");
        else
            setTitle("Edit Trigger");
    }

    @Override
    public void onSaveItem(Bundle data) {
/*
        long now = System.currentTimeMillis();
        String msg = null;
        if (!data.containsKey(DbHelper.TriggerColumns.MODIFY_DATE)) {
            data.putLong(DbHelper.TriggerColumns.MODIFY_DATE, now);
        }
        if (isInsert) {
            if (!data.containsKey(DbHelper.TriggerColumns.CREATE_DATE)) {
                data.putLong(DbHelper.TriggerColumns.CREATE_DATE, now);
            }
//            Uri uri = getContentResolver().insert(DbHelper.TriggerColumns.CONTENT_URI, data);
//            showToast(StringUtil.format("%s added", data.getString(DbHelper.TriggerColumns.NAME)));
        }
        else {
//            getContentResolver().update(getIntent().getData(), data, null, null);
//            showToast(StringUtil.format("%s updated", data.getString(DbHelper.TriggerColumns.NAME)));
        }
*/
        Intent i = new Intent();
        i.putExtras(data);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onEditCanceled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onValidationError(String msg) {
        showToast(msg);
    }
}
