package com.axway.apigwgcm.api;

import android.app.Activity;
import android.util.Log;

import com.axway.apigwgcm.util.StringUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by su on 4/21/2016.
 */
public abstract class ActivityCallback extends BaseCallback {

    private static final String TAG = ActivityCallback.class.getSimpleName();

    private WeakReference<Activity> actRef;

    private ActivityCallback() {
        super();
        actRef = null;
    }

    public ActivityCallback(Activity activity) {
        super();
        actRef = new WeakReference<>(activity);
    }

    private Activity getActivity() {
        if (actRef == null)
            return null;
        return actRef.get();
    }

    @Override
    public void onFailure(final Call call, final IOException e) {
        Activity a = getActivity();
        if (a == null)
            return;
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onFailureResponse(call, e);
            }
        });
    }

    @Override
    public void onResponse(final Call call, final Response response) {
        if (response.isSuccessful()) {
            Log.d(TAG, StringUtil.format("success: %s", response));
            String bdy = null;
            try {
                bdy = response.body().string();
            }
            catch (IOException e) {
                bdy = null;
                Log.e(TAG, "unexpected exception while reading body", e);
            }
            finally {
                response.body().close();
            }
            final String body = bdy;
            Activity a = getActivity();
            if (a == null)
                return;
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onSuccessResponse(response.code(), response.message(), body);
                }
            });
        }
        else {
            onFailure(call, new IOException(StringUtil.format("%d %s", response.code(), response.message())));
        }
    }

    protected void onFailureResponse(final Call call, final IOException e) {
        Log.e(TAG, StringUtil.format("call failed: %s", call), e);
    }

    abstract protected void onSuccessResponse(final int code, final String msg, final String body);
}
