package com.axway.apigwgcm.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.fragment.ManageCertsFragment;
import com.axway.apigwgcm.util.StringUtil;

import java.util.Locale;

/**
 * Created by su on 11/19/2014.
 */
public class ManageCertsActivity extends BaseActivity implements ManageCertsFragment.Callbacks {

    private static final String TAG = ManageCertsActivity.class.getSimpleName();

    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tb_frame);
        title = getString(R.string.trusted_certs);
        replaceFragment(R.id.container01, ManageCertsFragment.newInstance(), Constants.TAG_SINGLE_PANE);
    }

    @Override
    public void onRemoveKeystore() {
        Log.d(TAG, "onRemoveKeystore");
        confirmDialog(getString(R.string.confirm_msg, "remove Trusted Certificate Store"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performRemoveKeystore();
            }
        });
    }

    @Override
    public void onRowsCounted(int n) {
        setTitle(title);
        setSubtitle(StringUtil.format("%d row%s", n, (n == 1 ? "" : "s")));
    }

    private void performRemoveKeystore() {
        Log.d(TAG, "performRemoveKeystore");
        BaseApp.keystoreManager().removeKeystore();
        ManageCertsFragment frag = (ManageCertsFragment)findFragment(Constants.TAG_SINGLE_PANE);
        if (frag != null)
            frag.refresh();
        showToast(R.string.msg_keystore_removed);
    }

    @Override
    protected boolean navIsBack() {
        return true;
    }
}
