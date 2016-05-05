package com.axway.apigwgcm.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.SyncAdapter;
import com.axway.apigwgcm.api.ActivityCallback;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.TrigListFrag;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.GcmUtil;
import com.axway.apigwgcm.util.JsonUtil;
import com.axway.apigwgcm.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by su on 4/21/2016.
 */
public class TriggerListActivity extends OAuthBaseActivity {

    public static final String TAG = TriggerListActivity.class.getSimpleName();
    private BroadcastReceiver bcastRcvr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tb_frame);
        bcastRcvr = null;
        OAuthToken tkn = getAuthToken();
        if (tkn == null || tkn.isExpired()) {
            refreshToken();
            return;
        }
        getMsgHandler().sendEmptyMessage(MSG_TOKEN_OBTAINED);
    }

    @Override
    protected boolean onHandleMessage(Message msg) {
        if (msg.what == MSG_TOKEN_OBTAINED) {
            tokenObtained();
            return true;
        }
        return super.onHandleMessage(msg);
    }

    private void tokenObtained() {
        OAuthToken tkn = getAuthToken();
        Log.d(TAG, StringUtil.format("OAuthToken: %s", tkn));
        String[] parts = getAccountUtil().splitAccountName(getAccount());
        OkHttpClient cli = BaseApp.httpClient();
        Request req = new Request.Builder()
                .url(makeBaseUrl() + "/gcm/triggers/" + parts[AccountUtil.NDX_USERNAME])
                .header("Authorization", "Bearer " + tkn.getAccessToken())
                .build();
        baseApp.executeRequestAsync(req, new TriggersCallback(this));
    }

    private void onTriggersLoaded(JsonArray array) {
        Log.d(TAG, StringUtil.format("onTriggersLoaded: %d", (array == null ? 0 : array.size())));
        showProgressBar(false);
        setTitle(StringUtil.format("Event Triggers (%d)", (array == null ? 0 : array.size())));
        replaceFragment(R.id.container01, TrigListFrag.newInstance(array), Constants.TAG_SINGLE_PANE);
    }


    private class TriggersCallback extends ActivityCallback {

        public TriggersCallback(Activity activity) {
            super(activity);
        }

        @Override
        protected void onSuccessResponse(int code, String msg, String body) {
            JsonArray res = null;
            String innerName = "messages";
            JsonElement json = JsonUtil.getInstance().parse(body);
            if (json != null) {
                if (json.isJsonArray()) {
                    res = json.getAsJsonArray();
                }
                else if (json.isJsonObject() && !TextUtils.isEmpty(innerName)) {
                    JsonObject jo = json.getAsJsonObject();
                    if (jo.has(innerName) && jo.get(innerName).isJsonArray())
                        res = jo.getAsJsonArray(innerName);
                }
            }
            if (res == null)
                res = new JsonArray();
            onTriggersLoaded(res);
        }
    }
}
