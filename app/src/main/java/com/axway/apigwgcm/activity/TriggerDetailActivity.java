package com.axway.apigwgcm.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.TriggerDtlFragment;

/**
 * Created by su on 12/5/2014.
 */
public class TriggerDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tb_frame);
        if (getIntent().getData() == null) {
            showToast("invalid input: no uri specified");
            finish();
            return;
        }
        setTitle(DbHelper.getTitle(getIntent().getData()));
        final Fragment f = TriggerDtlFragment.newInstance(getIntent().getData());
        f.setArguments(intentToBundle());

        replaceFragment(R.id.container01, f, Constants.TAG_SINGLE_PANE);
    }
}
