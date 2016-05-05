package com.axway.apigwgcm.api;

import android.util.Log;

import com.axway.apigwgcm.util.StringUtil;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by su on 4/21/2016.
 */
public abstract class BaseCallback implements Callback {

    private static final String TAG = BaseCallback.class.getSimpleName();

    @Override
    public void onFailure(Call call, final IOException e) {
        Log.e(TAG, StringUtil.format("call failed: %s", call), e);
    }

    @Override
    public void onResponse(final Call call, final Response response) throws IOException {
        if (response.isSuccessful()) {
            Log.d(TAG, StringUtil.format("success: %s", response));
            String bdy = null;
            int cd = 0;
            String msg = null;
            try {
                cd = response.code();
                msg = response.message();
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
            onSuccessResponse(cd, msg, body);
/*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onSuccessResponse(response.code(), response.message(), body);
                }
            });
*/
        }
        else {
            onFailure(call, new IOException(StringUtil.format("%d %s", response.code(), (response.message()))));
        }
    }

    abstract protected void onSuccessResponse(int code, String msg, String body);
}
