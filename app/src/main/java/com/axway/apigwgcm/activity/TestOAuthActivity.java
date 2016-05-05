package com.axway.apigwgcm.activity;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.events.ActionEvent;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.squareup.otto.Subscribe;

/**
 * Created by su on 4/27/2016.
 */
public class TestOAuthActivity extends OAuthBaseActivity {
    private static final String TAG = TestOAuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tb_frame);
        OAuthToken t = getAuthToken();
        if (t == null || t.isExpired()) {
            refreshToken();
        }
        getMsgHandler().sendEmptyMessage(MSG_TOKEN_OBTAINED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "unregister with event bus");
        BaseApp.bus().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "register with event bus");
        BaseApp.bus().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;    //super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActionEvent evt = new ActionEvent(item.getItemId());
        BaseApp.post(evt);
        return true;    //super.onOptionsItemSelected(item);
    }

    private void tokenObtained() {
        OAuthToken t = getAuthToken();
        if (t == null) {
            Log.d(TAG, "tokenObtained but it's null");
            return;
        }
        showToast("Token obtained!");
    }

    @Override
    protected boolean onHandleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TOKEN_OBTAINED:
                tokenObtained();
                return true;
        }
        return super.onHandleMessage(msg);
    }

    @Subscribe
    public void onEvent(ActionEvent evt) {
        Log.d(TAG, String.format("actionEvent: %d", evt.id));
        switch (evt.id) {
            case R.id.action_invalidate_token:
                confirmInvalidateToken(false);
                break;
            case R.id.action_token_info:
                getTokenInfo();
                break;
            case R.id.action_revoke_token:
                confirmRevokeToken();
                break;
        }
    }
}
