package com.axway.apigwgcm.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.fragment.SettingsFragment;

/**
 * Created by su on 11/19/2014.
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tb_frame);
        if (savedInstanceState == null) {
            Fragment frag = SettingsFragment.newInstance();
            replaceFragment(R.id.container01, frag, Constants.TAG_SINGLE_PANE);
        }
    }

    @Override
    protected boolean navIsBack() {
        return true;
    }
}
