package com.axway.apigwgcm.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.CursorFragment;
import com.axway.apigwgcm.fragment.MessageDtlFragment;


/**
 * Created by su on 12/5/2014.
 */
public class MessageDetailActivity extends BaseActivity {

    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() == null) {
            showToast("invalid input: no uri specified");
            finish();
            return;
        }
        title = DbHelper.getTitle(getIntent().getData());
        setContentView(R.layout.tb_frame);
        final Fragment f = MessageDtlFragment.newInstance(getIntent().getData());
        replaceFragment(R.id.container01, f, Constants.TAG_SINGLE_PANE);
    }

    @Override
    protected void setupToolbar() {
        if (!TextUtils.isEmpty(title))
            toolbar.setTitle(title);
    }
}
