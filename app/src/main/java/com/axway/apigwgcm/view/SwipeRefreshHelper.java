package com.axway.apigwgcm.view;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;

import com.axway.apigwgcm.R;

/**
 * Created by su on 12/16/2014.
 */
public class SwipeRefreshHelper {

    private static final String TAG = SwipeRefreshHelper.class.getSimpleName();

    private static final long DEF_DISABLE_MILLIS = 5000;

    private SwipeRefreshLayout swipeRefresh;
    private long disableMillis;

    public SwipeRefreshHelper(final Activity activity, final SwipeRefreshLayout.OnRefreshListener listener) {
        super();
        disableMillis = DEF_DISABLE_MILLIS;
        if (activity != null) {
            swipeRefresh = (SwipeRefreshLayout) activity.findViewById(R.id.ctr_swipe_refresh);
            if (swipeRefresh != null)
                swipeRefresh.setOnRefreshListener(listener);
        }
    }

    public void showRefreshing(final boolean show) {
        if (swipeRefresh == null)
            return;
        swipeRefresh.setRefreshing(show);
    }


    public void enableRefreshing(final boolean enable) {
        enableRefreshing(enable, true);
    }

    public void enableRefreshing(final boolean enable, final boolean delayedEnable) {
        if (swipeRefresh == null)
            return;
        swipeRefresh.setEnabled(enable);
        if (!enable && delayedEnable) {
            swipeRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setEnabled(true);
                }
            }, getDisableMillis());
        }
    }

    public long getDisableMillis() {
        return disableMillis;
    }

    public void setDisableMillis(long newVal) {
        this.disableMillis = newVal;
    }
}
