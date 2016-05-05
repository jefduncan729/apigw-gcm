package com.axway.apigwgcm.fragment;

import android.app.Activity;
import android.app.Fragment;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.oauth.OAuthClient;

import java.lang.ref.WeakReference;

/**
 * Created by su on 4/27/2016.
 */
public class OAuthFragment extends Fragment {
    private WeakReference<Activity> actRef;
    private OAuthClient oAuthClient;

    public static OAuthFragment newInstance(Activity a) {
        OAuthFragment rv = new OAuthFragment();
        rv.actRef = new WeakReference<Activity>(a);
        rv.oAuthClient = new OAuthClient(Constants.API_GATEWAY_CLIENT_ID, Constants.API_GATEWAY_CLIENT_SECRET);
        rv.setRetainInstance(true);
        return rv;
    }
}
